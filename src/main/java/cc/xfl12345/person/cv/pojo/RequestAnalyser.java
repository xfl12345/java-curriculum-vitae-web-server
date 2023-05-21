package cc.xfl12345.person.cv.pojo;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class RequestAnalyser {
    protected LinkedHashMap<String, IpAddressGetter> ipAddressGetters = new LinkedHashMap<>();

    public RequestAnalyser() {
    }

    @PostConstruct
    public void init() {

        Consumer<SimpleIpAddressGetter> putIntoMap = (getter) -> {
            ipAddressGetters.put(getter.getHeaderKey(), getter);
        };

        Consumer<String> generateGetterViaHeaderThenPutIntoMap = (header) -> {
            putIntoMap.accept(new SimpleIpAddressGetter(header));
        };

        generateGetterViaHeaderThenPutIntoMap.accept("cf-connecting-ip");

        putIntoMap.accept(new SimpleIpAddressGetter("X-Forwarded-For") {
            @Override
            protected String getIpAddress(String headerContent) {
                String ipAddr = null;
                int index = headerContent.indexOf(',');
                if (index != -1) {
                    // 只获取第一个值
                    ipAddr = headerContent.substring(0, index);
                } else {
                    ipAddr = headerContent;
                }

                return ipAddr;
            }
        });

        generateGetterViaHeaderThenPutIntoMap.accept("X-Real-IP");
        generateGetterViaHeaderThenPutIntoMap.accept("REMOTE-HOST");
    }


    /**
     * 获取客户端真实IP地址
     *
     * @param request http请求
     * @return IP地址字符串
     */
    public String getIpAddress(HttpServletRequest request) {
        for (IpAddressGetter getter : ipAddressGetters.values()) {
            String ipAddr = getter.getIpAddress(request);
            if (ipAddr != null) {
                return ipAddr;
            }
        }

        return request.getRemoteAddr();
    }

}
