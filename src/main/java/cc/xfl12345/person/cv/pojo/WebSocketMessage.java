package cc.xfl12345.person.cv.pojo;

import lombok.Data;

@Data
public class WebSocketMessage {

    private Type messageType;

    private Object payload;

    public enum Type {
        request,
        response
    }
}
