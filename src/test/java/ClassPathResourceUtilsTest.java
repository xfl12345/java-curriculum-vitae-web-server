import cc.xfl12345.person.cv.utility.ClassPathResourceUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class ClassPathResourceUtilsTest {
    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("Hello,world!");

        ObjectMapper objectMapper = new ObjectMapper();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // URL url = classLoader.getResource("META-INF/maven/cloud.tianai.captcha/tianai-captcha/");
        //
        // if (url == null) {
        //     throw new FileNotFoundException("URL is not exist!");
        // }
        //
        // JarURLConnection specRootJarURLConnection = (JarURLConnection) url.openConnection();
        // System.out.println("specRootJarURLConnection.getEntryName()=" + specRootJarURLConnection.getEntryName());
        //
        // System.out.println(url);
        // System.out.println("URL target content first line=" + readOneLineFromStream(new URL(url, "pom.properties")));

        Map<String, Map<String, URL>> resources = ClassPathResourceUtils.getURL(
            "META-INF/maven/cloud.tianai.captcha/tianai-captcha/",
            null,
            null,
            true
        );
        System.out.println(objectMapper.valueToTree(resources).toPrettyString());

        long maxTime = 0;
        long minTime = Long.MAX_VALUE;
        long totalTime = 0;
        long avgTime = 0;
        int count = 300;
        int fileCount = 0;

        for (int i = 0; i < count; i++) {
            Date startTime = new Date();
            resources = ClassPathResourceUtils.getURL(
                "org",
                // "META-INF",
                null,
                null,
                true
            );
            Date endTime = new Date();
            long speet = endTime.getTime() - startTime.getTime();
            if (speet > maxTime) {
                maxTime = speet;
            }
            if (speet < minTime) {
                minTime = speet;
            }
            totalTime += speet;
        }

        avgTime = totalTime / count;
        System.out.println("fileCount=" + resources.values().parallelStream().mapToLong(Map::size).sum());
        System.out.println("min=" + minTime  + "ms, max=" + maxTime + "ms, avg=" + avgTime + "ms");

        // System.out.print("\n".repeat(10));
        // System.out.println(objectMapper.valueToTree(resources).toPrettyString());


        // resources = ClassPathResourceUtils.getURL(
        //     "META-INF/spring",
        //     null,
        //     null,
        //     true
        // );
        // System.out.println(objectMapper.valueToTree(resources).toPrettyString());
        //
        // resources = ClassPathResourceUtils.getURL(
        //     "META-INF/maven",
        //     null,
        //     null,
        //     false
        // );
        // System.out.println(objectMapper.valueToTree(resources).toPrettyString());
        //
        // resources = ClassPathResourceUtils.getURL(
        //     "cc/xfl12345/person/cv",
        //     null,
        //     null,
        //     false
        // );
        // System.out.println(objectMapper.valueToTree(resources).toPrettyString());
        //
        // resources = ClassPathResourceUtils.getURL(
        //     "cc/xfl12345/person/cv/",
        //     null,
        //     null,
        //     false
        // );
        // System.out.println(objectMapper.valueToTree(resources).toPrettyString());
        //
        // resources = ClassPathResourceUtils.getURL(
        //     "cc/xfl12345/person/cv/picture",
        //     null,
        //     null,
        //     true
        // );
        // System.out.println(objectMapper.valueToTree(resources).toPrettyString());
    }

    public static String readOneLineFromStream(URL url) throws IOException {
        InputStream inputStream = url.openConnection().getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try( bufferedReader; inputStreamReader; inputStream ) {
            return bufferedReader.readLine();
        }
    }
}
