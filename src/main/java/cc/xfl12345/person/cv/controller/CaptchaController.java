package cc.xfl12345.person.cv.controller;


import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.request.GenericBaseRequestObject;
import cc.xfl12345.person.cv.pojo.request.payload.PhoneNumberData;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cc.xfl12345.person.cv.service.SMS;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("captcha")
public class CaptchaController {

    private ImageCaptchaApplication imageCaptchaApplication;

    private ObjectMapper objectMapper;

    private SMS SMS;

    @Autowired
    public void setImageCaptchaApplication(ImageCaptchaApplication imageCaptchaApplication) {
        this.imageCaptchaApplication = imageCaptchaApplication;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setSmsService(SMS SMS) {
        this.SMS = SMS;
    }

    @PostConstruct
    public void init() {
    }

    @GetMapping("generate")
    @ResponseBody
    public CaptchaResponse<ImageCaptchaVO> genCaptcha(@RequestParam(value = "type", required = false)String type) {
        if (StringUtils.isBlank(type)) {
            type = CaptchaTypeConstant.SLIDER;
        }
        return imageCaptchaApplication.generateCaptcha(type);
    }

    @PostMapping("check")
    @ResponseBody
    public JsonApiResponseData checkCaptcha(HttpServletRequest request, @RequestParam("id") String id, @RequestBody ImageCaptchaTrack imageCaptchaTrack) {
        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        boolean currentResult = imageCaptchaApplication.matching(id, imageCaptchaTrack);

        if (currentResult) {
            responseData.setApiResult(JsonApiResult.SUCCEED);
            if (imageCaptchaTrack.getData() != null) {
                JsonApiResponseData responseDataPayload = new JsonApiResponseData(JsonApiConst.VERSION, JsonApiResult.FAILED_NOT_SUPPORT);
                try {
                    GenericBaseRequestObject<?> extraData = objectMapper.convertValue(imageCaptchaTrack.getData(), GenericBaseRequestObject.class);
                    if (extraData != null) {
                        if (extraData.operation != null) {
                            String operation = extraData.operation;
                            // 请求拉取短信验证码
                            if (operation.equals("pull-sms-verification-code")) {
                                responseDataPayload = SMS.sendValidationCode(request, objectMapper.convertValue(extraData.data, PhoneNumberData.class));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // 负载数据格式错误
                    responseDataPayload.setApiResult(JsonApiResult.FAILED_REQUEST_FORMAT_ERROR);
                }

                responseData.setData(responseDataPayload);
            }
        } else {
            // 人机验证不通过
            responseData.setApiResult(JsonApiResult.FAILED_FORBIDDEN);
        }

        return responseData;
    }

    /**
     * 二次验证，一般用于机器内部调用，这里为了方便测试
     * @param id id
     * @return boolean
     */
    @GetMapping("check2")
    @ResponseBody
    public boolean check2Captcha(@RequestParam("id") String id) {
        // 如果开启了二次验证
        if (imageCaptchaApplication instanceof SecondaryVerificationApplication) {
            return ((SecondaryVerificationApplication) imageCaptchaApplication).secondaryVerification(id);
        }
        return false;
    }
}
