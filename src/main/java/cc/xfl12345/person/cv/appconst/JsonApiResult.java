package cc.xfl12345.person.cv.appconst;

import org.springframework.http.HttpStatus;

public enum JsonApiResult {
    SUCCEED("成功", HttpStatus.OK.value()),
    FAILED("失败", HttpStatus.FORBIDDEN.value()),
    FAILED_INVALID("参数无效", HttpStatus.FORBIDDEN.value()),
    FAILED_MISSING_PARAMS("缺少参数", HttpStatus.FORBIDDEN.value()),
    FAILED_TOO_MUCH_PARAMS("参数过多", HttpStatus.FORBIDDEN.value()),
    FAILED_FORBIDDEN("非法操作", HttpStatus.FORBIDDEN.value()),
    FAILED_NOT_SUPPORT("操作不支持", HttpStatus.FORBIDDEN.value()),
    FAILED_NOT_FOUND("请求资源不存在", HttpStatus.NOT_FOUND.value()),
    FAILED_FREQUENCY_MAX("操作过于频繁", HttpStatus.TOO_MANY_REQUESTS.value()),
    FAILED_NO_LOGIN("未登录", HttpStatus.UNAUTHORIZED.value()),
    FAILED_FORBIDDEN_ACCOUNT("账号已被停用", HttpStatus.UNAUTHORIZED.value()),
    FAILED_LOGOUT_IS_NEEDED_BEFORE_LOGIN("登录失败！您已登录，如需登录其它账号，请先注销当前账号！", HttpStatus.FORBIDDEN.value()),
    FAILED_ALREADY_LOGIN_BY_OTHER("登录失败！账号已被他人登录！", HttpStatus.FORBIDDEN.value()),
    FAILED_REQUEST_FORMAT_ERROR("请求数据格式错误", HttpStatus.UNPROCESSABLE_ENTITY.value()),
    OTHER_FAILED("发生未知错误！请联系站点管理员修复。", HttpStatus.INTERNAL_SERVER_ERROR.value());

    private final String name;
    private final int num;

    JsonApiResult(String str, int num) {
        this.name = str;
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }
}
