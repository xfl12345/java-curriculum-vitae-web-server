package cc.xfl12345.person.cv.utility;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

@Slf4j
public class ClassPathResourceUtils {
    private static final URL HACK_URL;

    static {
        try {
            HACK_URL = new URL("jar:file://somehost/somejar.jar!/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class PathDetail {
        private final String path;
        private final String relativePath;

        private final String fileName;

        private final boolean directory;

        private final boolean file;

        public PathDetail(String path, String relativePath, String fileName, boolean isDirectory, boolean isFile) {
            this.path = path;
            this.relativePath = relativePath;
            this.fileName = fileName;
            this.directory = isDirectory;
            this.file = isFile;
        }

        public String getPath() {
            return path;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isDirectory() {
            return directory;
        }

        public boolean isFile() {
            return file;
        }
    }

    public static class UrlPathDetail extends PathDetail {
        private final URL url;

        public UrlPathDetail(URL url, String path, String relativePath, String fileName, boolean isDirectory, boolean isFile) {
            super(path, relativePath, fileName, isDirectory, isFile);
            this.url = url;
        }

        public URL getUrl() {
            return url;
        }
    }

    private static class JarFileUrlCache {
        private final JarURLConnection jarURLConnection;

        private final JarFile jarFile;

        private final URL url;

        private final URL jarFileRootUrl;

        public JarFileUrlCache(JarURLConnection jarURLConnection, boolean crudeMode) throws IOException {
            this.jarURLConnection = jarURLConnection;
            this.jarFile = jarURLConnection.getJarFile();
            this.url = jarURLConnection.getURL();

            JarEntry jarEntry = jarURLConnection.getJarEntry();
            if (jarEntry == null) {
                this.jarFileRootUrl = this.url;
            } else {
                String jarEntryName = jarEntry.getName();

                if (crudeMode) {
                    String urlInString = url.toString();
                    String rootUrlInString = urlInString.substring(0, urlInString.length() - jarEntryName.length());
                    this.jarFileRootUrl = new URL(rootUrlInString);
                } else {
                    int folderCount = 0;
                    for (int i = 0; i < jarEntryName.length(); i += 1) {
                        if (jarEntryName.charAt(i) == '/') {
                            folderCount += 1;
                        }
                    }
                    String goToRootPath = "../".repeat(folderCount);
                    this.jarFileRootUrl = new URL(url, goToRootPath);
                }

            }
        }

        public JarURLConnection getJarURLConnection() {
            return jarURLConnection;
        }

        public JarFile getJarFile() {
            return jarFile;
        }

        public URL getUrl() {
            return url;
        }

        public URL getJarFileRootUrl() {
            return jarFileRootUrl;
        }
    }

    public static String getJarFileUriRelativizePath(URI base, URI child) throws URISyntaxException {
        return (new URI(base.getRawSchemeSpecificPart())).relativize(new URI(child.getRawSchemeSpecificPart())).getPath();
    }

    public static String getFileUriRelativizePath(URI base, URI child) {
        return base.relativize(child).getPath();
    }

    public static FileSystem getFileSystemViaURL(URL url, ClassLoader classLoader) {
        try {
            return FileSystems.getFileSystem(url.toURI());
        } catch (URISyntaxException | FileSystemNotFoundException e) {
            // ignore
        }

        FileSystem fileSystem = null;
        try {
            String protocol = url.getProtocol();
            fileSystem = FileSystems.newFileSystem(URI.create(protocol + ":/"), Map.of(), classLoader);

            // 注册 JVM 关闭钩子。有始有终。
            FileSystem finalFileSystem = fileSystem;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try{
                    Class<?> fileSystemClass = finalFileSystem.getClass();
                    finalFileSystem.close();
                    //do something
                    System.out.println(String.format("FileSystem [%s] closed safety.", fileSystemClass.getCanonicalName()));
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException | FileSystemAlreadyExistsException e) {
            // ignore
        }

        if (fileSystem == null) {
            try {
                fileSystem = FileSystems.getFileSystem(url.toURI());
            } catch (URISyntaxException | FileSystemNotFoundException e) {
                // ignore
            }
        }

        return fileSystem;
    }

    private static Map<String, URL> internalFindResourceViaFile(
        final File originRoot,
        final File currentRoot,
        final String baseClassPath,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive,
        boolean isGetURL) throws MalformedURLException {
        // 如果不存在，直接返回
        if (!currentRoot.exists()) {
            return Collections.emptyMap();
        }

        // 目标对象就是文件，不需要遍历
        if (originRoot.isFile()) {
            String fileName = originRoot.getName();
            URI fileURI = originRoot.toURI();
            URL fileURL = isGetURL ? fileURI.toURL() : HACK_URL;
            if (filter.test(new UrlPathDetail(fileURL, baseClassPath, "", fileName, false, true))) {
                return Map.of(baseClassPath, fileURL);
            }

            return Collections.emptyMap();
        }

        Map<String, URL> urls = new ConcurrentHashMap<>();
        String[] fileNames = currentRoot.list();
        if (fileNames != null) {
            try {
                // 遍历所有文件
                Arrays.asList(fileNames).parallelStream().forEach(fileName -> {
                    try {
                        File file = new File(currentRoot, fileName);
                        // 如果是个文件夹
                        if (file.isDirectory()) {
                            if (recursive) {
                                urls.putAll(internalFindResourceViaFile(originRoot, file, baseClassPath, filter, recursive, isGetURL));
                            }
                        } else {
                            URI fileURI = file.toURI();
                            URL fileURL = isGetURL ? fileURI.toURL() : HACK_URL;
                            String relativeFilePath = getFileUriRelativizePath(originRoot.toURI(), fileURI);
                            String path = baseClassPath + '/' + relativeFilePath;
                            if (filter.test(new UrlPathDetail(fileURL, path, relativeFilePath, fileName, file.isDirectory(), file.isFile()))) {
                                urls.put(path, fileURL);
                            }
                        }
                    } catch (MalformedURLException urlException) {
                        throw new RuntimeException(urlException);
                    }
                });
            } catch (RuntimeException runtimeException) {
                Throwable throwable = runtimeException.getCause();
                if (throwable instanceof MalformedURLException urlException) {
                    throw urlException;
                }
                throw runtimeException;
            }
        }

        return urls;
    }


    public static Map<String, URL> findResourceUrlViaJarURLConnection(
        JarURLConnection jarURLConnection,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive,
        boolean crudeMode) throws IOException {
        return internalFindResourceViaJarURLConnection(
            jarURLConnection,
            filter,
            recursive,
            crudeMode,
            true
        );
    }

    public static Set<String> findResourcePathViaJarURLConnection(
        JarURLConnection jarURLConnection,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive,
        boolean crudeMode) throws IOException {
        return internalFindResourceViaJarURLConnection(
            jarURLConnection,
            filter,
            recursive,
            crudeMode,
            false
        ).keySet();
    }

    private static Map<String, URL> internalFindResourceViaJarURLConnection(
        JarURLConnection jarURLConnection,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive,
        boolean crudeMode,
        boolean isGetURL) throws IOException {
        // 注意！这里不能使用 jarURLConnection.getEntryName() ！因为无法判断是否是目录。
        // 使用 jarURLConnection.getJarEntry().getName() ，如果目标对象是目录，将自动补全末尾的左斜杠'/'
        String originRoot = jarURLConnection.getJarEntry().getName();

        // 判断一下是否是目录
        if (originRoot.length() > 0 && originRoot.charAt(originRoot.length() - 1) == '/') {
            URL originURL = jarURLConnection.getURL();
            String originUrlInString = originURL.toString();
            // 判断原 URL 是否以 左斜杠'/' 结尾。若不是，则需补上，以此基础重新生成 URL 。
            if (originUrlInString.charAt(originUrlInString.length() - 1) != '/') {
                URL okURL = new URL(originURL, "./");
                jarURLConnection = (JarURLConnection) okURL.openConnection();
            }

            JarFileUrlCache jarFileUrlCache = new JarFileUrlCache(jarURLConnection, crudeMode);

            return internalFindResourceUrlViaJarURLConnection(
                jarFileUrlCache,
                originRoot,
                filter,
                recursive,
                isGetURL
            );

        } else {
            // 不是目录，直接返回该URL
            Map<String, URL> urls = new HashMap<>(1);
            urls.put(originRoot, jarURLConnection.getURL());
            return urls;
        }
    }

    private static Map<String, URL> internalFindResourceUrlViaJarURLConnection(
        final JarFileUrlCache jarFileUrlCache,
        String originRoot,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive,
        boolean isGetURL) throws IOException {

        Map<String, URL> urls = new ConcurrentHashMap<>();

        try {
            JarFile jarFile = jarFileUrlCache.getJarFile();
            URL jarFileRootUrl = jarFileUrlCache.getJarFileRootUrl();

            // Java 17 底层实现是加了 synchronized 同步锁的，并行操作可能意义不大
            // 目前考虑先用CPU单核心加载全部 JarEntry ，然后再用CPU多核心并行遍历全部 JarEntry
            // 耗时可能变成 O(2n) ，但应该取决于 JarEntry 数量和 CPU核心数量， 两者越大可能耗时越少。
            Collections.list(jarFile.entries()).parallelStream().forEach(jarEntry -> {
                // jarFile.stream().parallel().forEach(jarEntry -> {
                // jarFile.stream().forEach(jarEntry -> {
                try {
                    String jarFileInternalPath = jarEntry.getName();
                    // 切入指定前缀
                    if (jarFileInternalPath.startsWith(originRoot)) {
                        // 提取当前对象相对于给定根的路径
                        // jarFileInternalPath 是以 Jar 包为根的路径
                        // 后面的 +1 是为了去掉一定会有的 '/'
                        String relativeFilePath = jarFileInternalPath.substring(originRoot.length());
                        // 若是指定的根目录
                        if (relativeFilePath.length() != 0) {
                            int lastIndexOfSplitChar = relativeFilePath.lastIndexOf('/');
                            // 是否位于当前目录
                            boolean isInCurrentFolder = lastIndexOfSplitChar < 0;
                            // // 如果不是以 左斜杠'/'，则是文件
                            // boolean isFile = relativeFilePath.charAt(relativeFilePath.length() - 1) != '/';
                            boolean isDirectory = jarEntry.isDirectory();
                            boolean isFile = !isDirectory;
                            // 以下两种情况会被允许进入分支。
                            // 1.允许递归（无需考虑是否位于子目录）
                            // 2.位于当前目录的对象（无需考虑是否允许递归 ）
                            // 当且仅当 “不允许递归” 又 “对象位于子目录”，才会被拒绝进入分支。
                            if (recursive || isInCurrentFolder) {
                                String fileName = isFile ?
                                    relativeFilePath : relativeFilePath.substring(lastIndexOfSplitChar + 1);
                                // jar包根目录的URL + jar包内路径 = 目标文件路径
                                URL fileURL = isGetURL ? new URL(jarFileRootUrl, jarFileInternalPath) : HACK_URL;
                                if (filter.test(new UrlPathDetail(fileURL, jarFileInternalPath, relativeFilePath, fileName, isDirectory, isFile))) {
                                    urls.put(jarFileInternalPath, fileURL);
                                }
                            }
                        } else {
                            URL fileURL = isGetURL ? new URL(jarFileRootUrl, jarFileInternalPath) : HACK_URL;
                            if (filter.test(new UrlPathDetail(fileURL, jarFileInternalPath, relativeFilePath, "", true, false))) {
                                urls.put(jarFileInternalPath, fileURL);
                            }
                        }
                    }
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            });
        } catch (RuntimeException runtimeException) {
            if (runtimeException.getCause() instanceof IOException e) {
                throw e;
            }
            throw runtimeException;
        }

        return urls;
    }


    public static Map<String, URL> findResourceUrlViaFileSystem(
        final FileSystem fileSystem,
        final String rootPath,
        ClassLoader classLoader,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive) throws IOException {
        return internalFindResourceUrlViaFileSystem(
            fileSystem,
            rootPath,
            classLoader,
            filter,
            recursive,
            true
        );
    }

    public static Set<String> findResourcePathViaFileSystem(
        final FileSystem fileSystem,
        final String rootPath,
        ClassLoader classLoader,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive) throws IOException {
        return internalFindResourceUrlViaFileSystem(
            fileSystem,
            rootPath,
            classLoader,
            filter,
            recursive,
            false
        ).keySet();
    }

    private static Map<String, URL> internalFindResourceUrlViaFileSystem(
        final FileSystem fileSystem,
        final String rootPath,
        ClassLoader classLoader,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive,
        boolean isGetURL) throws IOException {

        Map<String, URL> urls = new ConcurrentHashMap<>();

        Path fileSystemPath = fileSystem.getPath(rootPath);
        try (Stream<Path> pathStream = Files.walk(fileSystemPath).parallel()) {
            if ("".equals(rootPath)) {
                pathStream.forEach(path -> {
                    String theClassPath = path.toString();
                    Path pathFileName = path.getFileName();
                    String fileName = pathFileName == null ? "" : pathFileName.toString();
                    int lastIndexOfSplitChar = theClassPath.lastIndexOf('/');
                    // 是否位于当前目录
                    boolean isInCurrentFolder = lastIndexOfSplitChar < 0;
                    if (recursive || isInCurrentFolder) {
                        URL fileURL = isGetURL ? classLoader.getResource(theClassPath) : HACK_URL;
                        if (fileURL != null && filter.test(new UrlPathDetail(fileURL, theClassPath, theClassPath, fileName, Files.isDirectory(path), Files.isRegularFile(path)))) {
                            urls.put(theClassPath, fileURL);
                        }
                    }
                });

            } else {
                pathStream.forEach(path -> {
                    String theClassPath = path.toString();
                    if (theClassPath.startsWith(rootPath)) {
                        String relativeFilePath = theClassPath.substring(rootPath.length());
                        Path pathFileName = path.getFileName();
                        String fileName = pathFileName == null ? "" : pathFileName.toString();
                        if (relativeFilePath.length() != 0) {
                            int lastIndexOfSplitChar = relativeFilePath.lastIndexOf('/');
                            // 是否位于当前目录
                            boolean isInCurrentFolder = lastIndexOfSplitChar < 0;
                            if (recursive || isInCurrentFolder) {
                                URL fileURL = isGetURL ? classLoader.getResource(theClassPath) : HACK_URL;
                                if (fileURL != null && filter.test(new UrlPathDetail(fileURL, theClassPath, relativeFilePath, fileName, Files.isDirectory(path), Files.isRegularFile(path)))) {
                                    urls.put(theClassPath, fileURL);
                                }
                            }
                        } else {
                            URL fileURL = isGetURL ? classLoader.getResource(theClassPath) : HACK_URL;
                            if (fileURL != null && filter.test(new UrlPathDetail(fileURL, rootPath, relativeFilePath, fileName, Files.isDirectory(path), Files.isRegularFile(path)))) {
                                urls.put(rootPath, fileURL);
                            }
                        }
                    }
                });
            }
        }

        return urls;
    }

    /**
     * @return Map(path, Map ( relativePath, URL))
     */
    public static Map<String, Map<String, URL>> getURL(
        String path,
        ClassLoader classLoader,
        Predicate<? super UrlPathDetail> filter,
        boolean recursive) throws IOException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (filter == null) {
            filter = (item) -> true;
        }
        final Predicate<? super UrlPathDetail> finalFilter = filter;

        Module currentClassLoaderModule = classLoader.getClass().getModule();
        boolean crudeMode = currentClassLoaderModule != null &&
            currentClassLoaderModule.getName() != null &&
            currentClassLoaderModule.getName().startsWith("java");

        // 定义一个枚举的集合 并进行循环来处理这个目录下的东西
        Enumeration<URL> dirs = classLoader.getResources(path);
        Map<String, Map<String, URL>> urls = new ConcurrentHashMap<>();

        List<URL> dirsList = Collections.list(dirs);

        // 遍历
        try {
            ClassLoader finalClassLoader = classLoader;
            dirsList.parallelStream().forEach(url -> {
                Map<String, URL> map = Collections.emptyMap();
                try {
                    // 得到协议的名称
                    String protocol = url.getProtocol();
                    // 如果是以文件的形式保存在服务器上
                    if ("file".equals(protocol)) {
                        // 以文件的方式扫描整个classpath下的文件 并添加到集合中
                        File file = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
                        map = internalFindResourceViaFile(
                            file,
                            file,
                            path,
                            finalFilter,
                            recursive,
                            true
                        );
                    } else if ("jar".equals(protocol)) {
                        // 如果是jar包文件
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        map = findResourceUrlViaJarURLConnection(
                            jarURLConnection,
                            finalFilter,
                            recursive,
                            crudeMode
                        );
                    } else {
                        FileSystem fileSystem = Objects.requireNonNull(getFileSystemViaURL(url, finalClassLoader));
                        map = findResourceUrlViaFileSystem(fileSystem, path, finalClassLoader, finalFilter, recursive);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    urls.put(url.toString(), map);
                }
            });
        } catch (RuntimeException runtimeException) {
            Throwable throwable = runtimeException.getCause();
            if (throwable instanceof IOException ioException) {
                throw ioException;
            }

            throw runtimeException;
        }

        return urls;
    }


    public static Map<String, Map<String, URL>> getURL(String path) throws IOException {
        return getURL(path, null, null, true);
    }

    public static Map<String, Set<String>> listPath2File(
        String path,
        ClassLoader classLoader,
        Predicate<? super PathDetail> filter,
        boolean recursive) throws IOException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (filter == null) {
            filter = (item) -> true;
        }
        final Predicate<? super PathDetail> finalFilter = filter;

        Module currentClassLoaderModule = classLoader.getClass().getModule();
        boolean crudeMode = currentClassLoaderModule != null &&
            currentClassLoaderModule.getName() != null &&
            currentClassLoaderModule.getName().startsWith("java");

        // 定义一个枚举的集合 并进行循环来处理这个目录下的东西
        List<URL> dirs = Collections.list(classLoader.getResources(path));
        ConcurrentHashMap<String, Set<String>> paths = new ConcurrentHashMap<>();

        // 遍历
        try {
            ClassLoader finalClassLoader = classLoader;
            dirs.parallelStream().forEach(url -> {
                Set<String> set = Collections.emptySet();
                try {
                    // 得到协议的名称
                    String protocol = url.getProtocol();
                    // 如果是以文件的形式保存在服务器上
                    if ("file".equals(protocol)) {
                        // 以文件的方式扫描整个classpath下的文件 并添加到集合中
                        File file = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
                        set = internalFindResourceViaFile(
                            file,
                            file,
                            path,
                            finalFilter,
                            recursive,
                            false
                        ).keySet();
                    } else if ("jar".equals(protocol)) {
                        // 如果是jar包文件
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        set = findResourcePathViaJarURLConnection(
                            jarURLConnection,
                            finalFilter,
                            recursive,
                            crudeMode
                        );
                    } else {
                        FileSystem fileSystem = Objects.requireNonNull(getFileSystemViaURL(url, finalClassLoader));
                        set = findResourcePathViaFileSystem(fileSystem, path, finalClassLoader, finalFilter, recursive);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    paths.put(url.toString(), set);
                }
            });
        } catch (RuntimeException runtimeException) {
            Throwable throwable = runtimeException.getCause();
            if (throwable instanceof IOException ioException) {
                throw ioException;
            }

            throw runtimeException;
        }

        return paths;
    }


    public static Map<String, Set<String>> listPath2File(String path) throws IOException {
        return listPath2File(path, null, null, true);
    }
}
