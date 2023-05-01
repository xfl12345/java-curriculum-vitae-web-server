import cc.xfl12345.person.cv.utility.MyReflectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.teasoft.bee.osql.annotation.SysValue;
import org.teasoft.honey.osql.core.HoneyConfig;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeeSpringBootMetaDataConfGenerator {
    public static void main(String[] args) throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, String> beeProperties = new ConcurrentHashMap<>();
        Field[] fields = HoneyConfig.class.getDeclaredFields();

        List.of(fields).parallelStream().forEach(item -> {
            SysValue[] sysValues = item.getAnnotationsByType(SysValue.class);
            for (SysValue sysValue : sysValues) {
                String originValue = sysValue.value();
                Class<?> fieldType = item.getType();
                Class<?> wrapperType = MyReflectUtils.typeDic.get(fieldType.getTypeName());

                beeProperties.put(
                    originValue.substring(2, originValue.length() - 1),
                    wrapperType == null ?
                        item.getType().getCanonicalName() : wrapperType.getCanonicalName()
                );
            }
        });

        String honeyConfigClassName = HoneyConfig.class.getCanonicalName();
        List<Map<String, String>> beePropertiesSpringMetaData = beeProperties.keySet().parallelStream().map(key -> {
            Map<String, String> map = new HashMap<>(3);
            map.put("name", key);
            map.put("type", beeProperties.get(key));
            map.put("sourceType", honeyConfigClassName);
            return map;
        }).toList();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.set("properties", objectMapper.valueToTree(beePropertiesSpringMetaData));

        Thread.sleep(1000);
        System.out.println(objectMapper.valueToTree(objectNode).toPrettyString());



    }
}
