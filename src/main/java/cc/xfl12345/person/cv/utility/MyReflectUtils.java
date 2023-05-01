package cc.xfl12345.person.cv.utility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 这是一个通过Java反射机制获取变量的一个工具类
 * 方便获取类的字段名称，同时极大地减少字段以常量字符串形式写死在程序里
 */
public class MyReflectUtils {
    public static final HashMap<String, Class<?>> typeDic = new HashMap<>();

    static {
        typeDic.put(int.class.getName(), Integer.class);
        typeDic.put(double.class.getName(), Double.class);
        typeDic.put(float.class.getName(), Float.class);
        typeDic.put(long.class.getName(), Long.class);
        typeDic.put(short.class.getName(), Short.class);
        typeDic.put(byte.class.getName(), Byte.class);
        typeDic.put(boolean.class.getName(), Boolean.class);
        typeDic.put(char.class.getName(), Character.class);
    }

    /**
     * String 转 任意基本类型的包装类
     *
     * @param cls   基本类型
     * @param value String字符串
     * @return 基本类型的包装类的对象
     * @throws Exception 请务必保证目标类型有把String转成目标类型的构造方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T castToWrapperClassObject(Class<T> cls, String value) throws Exception {
        Class<?> wrapperClass = typeDic.get(cls.getName());
        if (wrapperClass == null) {
            return cls.getConstructor(String.class).newInstance(value);
        }
        return (T) wrapperClass.getConstructor(String.class).newInstance(value);
    }

    @SuppressWarnings("unchecked")
    public static  <T> Class<T> getGenericTypeFromRuntime(Class<?> cls, int typeArgumentIndex) {
        return (Class<T>) ((ParameterizedType) cls.getGenericSuperclass()).getActualTypeArguments()[typeArgumentIndex];
    }

    public static Field getFieldByName(Object obj, String FieldName) {
        Field res = null;
        try {
            res = obj.getClass().getDeclaredField(FieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Map<String, Object> obj2Map(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

    public static Object map2Obj(Map<String, Object> map, Class<?> cls) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Object obj = cls.getDeclaredConstructor().newInstance();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            field.set(obj, map.get(field.getName()));
        }
        return obj;
    }

    public static TreeSet<String> getFieldNamesAsTreeSet(Class<?> cls)
        throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return getFieldNames(cls, TreeSet::new);
    }

    public static ArrayList<String> getFieldNamesAsArrayList(Class<?> cls)
        throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return getFieldNames(cls, ArrayList::new);
    }

    public static <T extends Collection<String>> T getFieldNames(Class<?> cls, Supplier<T> supplier)
        throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Object obj = cls.getDeclaredConstructor().newInstance();
        T collection = supplier.get();
        Field[] fields = cls.getDeclaredFields(); // 获取所有成员对象及函数
        for (Field f : fields) {
            f.setAccessible(true);// 暴力反射。 私有的也可以被访问。
            collection.add(f.getName());
        }
        return collection;
    }

    public static Class<?> getClassByName(String className) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (Exception | Error e) {
            //ignore
        }

        if (clazz == null) {
            clazz = Class.forName(className);
        }

        return clazz;
    }

    /**
     * 从包package中获取所有的Class
     * =<a href="https://blog.csdn.net/jdzms23/article/details/17550119">source code URL</a>
     *
     * @param recursive 是否循环迭代
     */
    public static Collection<Class<?>> getClasses(
        String packageName,
        boolean recursive,
        boolean includeNestedClass) throws IOException, ClassNotFoundException {

        Collection<Class<?>> classes = new ConcurrentLinkedDeque<>();
        //获取包的名字 并进行替换
        PackageNameCache packageNameCache = new PackageNameCache(packageName);
        //定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;

        dirs = Thread.currentThread().getContextClassLoader().getResources(packageNameCache.getPackagePath());

        //遍历
        try {
            Collections.list(dirs).parallelStream().forEach(url -> {
                try {
                    //得到协议的名称
                    String protocol = url.getProtocol();
                    //如果是以文件的形式保存在服务器上
                    if ("file".equals(protocol)) {
                        //获取包的物理路径
                        String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                        //以文件的方式扫描整个包下的文件 并添加到集合中
                        classes.addAll(findClassesInPackageByFile(
                            packageNameCache,
                            new File(filePath),
                            recursive,
                            includeNestedClass
                        ));
                    } else if ("jar".equals(protocol)) {
                        //如果是jar包文件
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        //获取 JarFile
                        JarFile jar = jarURLConnection.getJarFile();
                        classes.addAll(findClassesInPackageByJar(
                            packageNameCache,
                            jar,
                            recursive,
                            includeNestedClass
                        ));
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException runtimeException) {
            if (runtimeException.getCause() instanceof ClassNotFoundException e) {
                throw e;
            } else if (runtimeException.getCause() instanceof IOException e) {
                throw e;
            }

            throw runtimeException;
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     * <a href="https://www.w3cschool.cn/article/29226523.html">source code URL</a>
     */
    public static Collection<Class<?>> findClassesInPackageByFile(
        final PackageNameCache packageNameCache,
        final File sourceRoot,
        final boolean recursive,
        final boolean includeNestedClass) throws ClassNotFoundException {
        ConcurrentLinkedDeque<Class<?>> classes = new ConcurrentLinkedDeque<>();
        // 如果不存在或者 也不是目录就直接返回
        if (!sourceRoot.exists() || !sourceRoot.isDirectory()) {
            return classes;
        }

        String[] fileNames = sourceRoot.list();

        if (fileNames != null) {
            try {
                // 遍历所有文件
                Arrays.asList(fileNames).parallelStream().forEach(fileName -> {
                    try {
                        File file = new File(sourceRoot, fileName);
                        String filePath = file.getPath();
                        // 如果是个文件夹
                        if (file.isDirectory()) {
                            if (recursive) {
                                classes.addAll(findClassesInPackageByFile(
                                    new PackageNameCache(packageNameCache.getPackageName() + '.' + fileName),
                                    file,
                                    recursive,
                                    includeNestedClass
                                ));
                            }
                        } else {
                            if (fileName.endsWith(".class")) {
                                String relativePath = filePath.substring(sourceRoot.getPath().length() + 1);
                                boolean isOK = true;
                                // 如果不允许递归，则检查是否存在子目录
                                if (!recursive) {
                                    // 同一个文件，如果没有子目录路径，应当内容相等。
                                    // 为提升性能，没必要检查内容一致，只需比对长度。
                                    isOK = relativePath.length() == fileName.length();
                                }

                                // 检查是否满足内部类要求
                                if (isOK && !includeNestedClass) {
                                    if (fileName.indexOf('$') >= 0) {
                                        isOK = false;
                                    }
                                }

                                if (isOK) {
                                    // java class 文件去掉后面的.class 只留下类名
                                    String classSimpleName = fileName.substring(0, fileName.length() - 6);

                                    String classPackageName = file.getPath().substring(
                                        sourceRoot.getPath().length() + 1
                                    );

                                    if (classPackageName.length() == file.getName().length()) {
                                        classPackageName = packageNameCache.getPackageName();
                                    } else {
                                        classPackageName = classPackageName
                                            .substring(0, classPackageName.length() - fileName.length())
                                            .replace('/', '.');
                                    }

                                    // 添加到集合中去
                                    classes.add(getClassByName(
                                        classPackageName + '.' + classSimpleName
                                    ));
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (RuntimeException runtimeException) {
                if (runtimeException.getCause() instanceof ClassNotFoundException e) {
                    throw e;
                } else {
                    throw runtimeException;
                }
            }
        }

        return classes;
    }

    /**
     * 以jar的形式来获取包下的所有Class
     */
    public static Collection<Class<?>> findClassesInPackageByJar(
        final PackageNameCache packageNameCache,
        final JarFile sourceRoot,
        final boolean recursive,
        final boolean includeNestedClass) throws ClassNotFoundException {
        ConcurrentLinkedDeque<Class<?>> classes = new ConcurrentLinkedDeque<>();

        String packageName = packageNameCache.getPackageName();
        String packagePath = packageNameCache.getPackagePath();

        try {
            sourceRoot.stream().parallel().forEach(jarEntry -> {
                String filePath = jarEntry.getName();
                // 如果前半部分和定义的包名相同 而且 是一个 class 文件
                if (filePath.startsWith(packagePath) && filePath.endsWith(".class")) {
                    String classSimpleName = null;
                    String classPackageName = null;

                    // 后面的 +1 是为了去掉一定会有的 '/'
                    String relativeFilePath = filePath.substring(packagePath.length() + 1);
                    int lastIndexOfSplitChar = relativeFilePath.lastIndexOf('/');
                    if (lastIndexOfSplitChar < 0) {
                        // 去掉后面的".class" 获取真正的类名
                        classSimpleName = relativeFilePath.substring(0, relativeFilePath.length() - 6);
                        classPackageName = packageName;
                    } else {
                        if (recursive) {
                            // 去掉后面的".class" 获取真正的类名
                            classSimpleName = relativeFilePath.substring(
                                lastIndexOfSplitChar + 1,
                                relativeFilePath.length() - 6
                            );
                            classPackageName = filePath.substring(0, packagePath.length() + 1);
                        }
                    }

                    if (classSimpleName != null && !"".equals(classSimpleName)) {
                        try {
                            if (classSimpleName.indexOf('$') < 0) {
                                // 添加到classes
                                classes.add(getClassByName(classPackageName + '.' + classSimpleName));
                            } else {
                                if (includeNestedClass) {
                                    // 添加到classes
                                    classes.add(getClassByName(
                                        classPackageName + '.' + classSimpleName
                                    ));
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        } catch (RuntimeException runtimeException) {
            if (runtimeException.getCause() instanceof ClassNotFoundException e) {
                throw e;
            } else {
                throw runtimeException;
            }
        }

        return classes;
    }

    public static class PackageNameCache {
        private final String packageName;

        private final String packagePath;

        public PackageNameCache(String packageName) {
            this.packageName = packageName;
            this.packagePath = packageName.replace('.', '/');
        }

        public PackageNameCache(JarEntry jarEntry) {
            this.packagePath = jarEntry.getName();
            this.packageName = packagePath.replace('/', '.');
        }

        public String getPackageName() {
            return packageName;
        }

        public String getPackagePath() {
            return packagePath;
        }
    }
}
