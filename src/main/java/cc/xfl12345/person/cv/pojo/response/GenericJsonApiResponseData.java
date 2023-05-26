package cc.xfl12345.person.cv.pojo.response;


import cc.xfl12345.person.cv.appconst.JsonApiResult;

public class GenericJsonApiResponseData <T> extends BaseResponseObject {
    protected int code;
    protected T data;

    public GenericJsonApiResponseData() {
        this.setVersion("undefined");//未定义版本号
    }

    public GenericJsonApiResponseData(String version) {
        this.setVersion(version);
    }

    public GenericJsonApiResponseData(JsonApiResult apiResult) {
        this();
        setApiResult(apiResult);
    }

    public GenericJsonApiResponseData(String version, JsonApiResult apiResult) {
        this(version);
        setApiResult(apiResult);
    }

    public void setApiResult(JsonApiResult apiResult) {
        this.setSuccess(apiResult.equals(JsonApiResult.SUCCEED));
        this.setCode(apiResult.getNum());
        this.setMessage(apiResult.getName());
    }

    public void appendMessage(String msg) {
        if (getMessage() == null) {
            setMessage(msg);
        } else {
            if (getMessage().equals("")) {
                setMessage(msg);
            } else {
                setMessage(getMessage() + ";" + msg);
            }
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
