package cc.xfl12345.person.cv.pojo.response;

import cc.xfl12345.person.cv.appconst.JsonApiResult;

public class JsonApiResponseData extends GenericJsonApiResponseData<Object> {
    public JsonApiResponseData() {
    }

    public JsonApiResponseData(String version) {
        super(version);
    }

    public JsonApiResponseData(JsonApiResult apiResult) {
        super(apiResult);
    }

    public JsonApiResponseData(String version, JsonApiResult apiResult) {
        super(version, apiResult);
    }
}
