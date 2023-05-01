import cc.xfl12345.person.cv.utility.MyReflectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collection;

public class MyReflectUtilsTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Hello,world!");

        ObjectMapper objectMapper = new ObjectMapper();
        Collection<Class<?>> classCollection = MyReflectUtils
            .getClasses("cc.xfl12345", true, true);

        System.out.println("[" + classCollection.size() + "] class found.");
        System.out.println(objectMapper.valueToTree(classCollection).toPrettyString());
    }

}
