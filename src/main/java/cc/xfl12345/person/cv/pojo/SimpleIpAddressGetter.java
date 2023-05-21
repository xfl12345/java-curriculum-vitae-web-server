package cc.xfl12345.person.cv.pojo;


import jakarta.servlet.http.HttpServletRequest;

public class SimpleIpAddressGetter implements IpAddressGetter {
    protected String headerKey;

    public String getHeaderKey() {
        return headerKey;
    }

    public SimpleIpAddressGetter(String headerKey) {
        this.headerKey = headerKey;
    }

    @Override
    public String getIpAddress(HttpServletRequest request) {
        String headerContent = request.getHeader(headerKey);
        if (headerContent != null && !"".equals(headerContent) && !"unknown".equalsIgnoreCase(headerContent)) {
            return getIpAddress(headerContent);
        }

        return null;
    }

    protected String getIpAddress(String headerContent) {
        return headerContent;
    }
}
