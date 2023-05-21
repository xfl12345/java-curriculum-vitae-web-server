package cc.xfl12345.person.cv.pojo;


import jakarta.servlet.http.HttpServletRequest;

public interface IpAddressGetter {
    String getIpAddress(HttpServletRequest request);
}
