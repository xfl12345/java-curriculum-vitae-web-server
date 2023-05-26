package cc.xfl12345.person.cv.controller;


import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.AnyUserRequestRateLimitHelper;
import cc.xfl12345.person.cv.pojo.AnyUserRequestRateLimitHelperFactory;
import cc.xfl12345.person.cv.pojo.RateLimitHelper;
import cc.xfl12345.person.cv.pojo.SimpleBucketConfigUtils;
import cc.xfl12345.person.cv.pojo.request.SmsVerificationCodeRequestData;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cc.xfl12345.person.cv.service.SmsService;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("captcha")
public class CaptchaController {

    private ImageCaptchaApplication imageCaptchaApplication;

    @Autowired
    public void setImageCaptchaApplication(ImageCaptchaApplication imageCaptchaApplication) {
        this.imageCaptchaApplication = imageCaptchaApplication;
    }

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private SmsService smsService;

    @Autowired
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }

    private AnyUserRequestRateLimitHelperFactory rateLimitHelperFactory;

    private AnyUserRequestRateLimitHelper pullSmsValidationCodeRateLimitHelper = null;

    @Autowired
    public void setRateLimitHelperFactory(AnyUserRequestRateLimitHelperFactory rateLimitHelperFactory) {
        this.rateLimitHelperFactory = rateLimitHelperFactory;
    }

    @PostConstruct
    public void init() {
        pullSmsValidationCodeRateLimitHelper = rateLimitHelperFactory.generate(
            "pullSmsValidationCodeRate",
            SimpleBucketConfigUtils.createConfigJustInMinutes(1),
            SimpleBucketConfigUtils.createConfigJustInMinutes(1)
        );
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
            try {
                SmsVerificationCodeRequestData extraData = objectMapper.treeToValue(objectMapper.valueToTree(imageCaptchaTrack.getData()), SmsVerificationCodeRequestData.class);
                if (extraData != null) {
                    String phoneNumber = extraData.data != null && extraData.data.phoneNumber != null ? extraData.data.phoneNumber : "";
                    String operation = extraData.operation != null ? extraData.operation : "";
                    if (operation.equals("")) {
                        // 无其它动作
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                    } else {
                        // 请求拉取短信验证码
                        if (operation.equals("pull-sms-verification-code") && !"".equals(phoneNumber)) {
                            RateLimitHelper.ConsumeResult consumeResult = pullSmsValidationCodeRateLimitHelper.tryConsume(request);
                            if (consumeResult.isSuccess()) {
                                try {
                                    currentResult = smsService.sendSmsValidationCode(phoneNumber);

                                    responseData.setApiResult(currentResult ? JsonApiResult.SUCCEED : JsonApiResult.FAILED);
                                    responseData.setData(Map.of(JsonApiConst.COOL_DOWN_REMAINDER_FIELD, consumeResult.getCoolDownRemainder()));
                                } catch (IOException e) {
                                    log.error(e.getMessage(), e.getCause());
                                    responseData.setApiResult(JsonApiResult.OTHER_FAILED);
                                }
                            } else {
                                responseData.setApiResult(JsonApiResult.FAILED_FREQUENCY_MAX);
                                responseData.setData(Map.of(JsonApiConst.COOL_DOWN_REMAINDER_FIELD, consumeResult.getCoolDownRemainder()));
                            }
                        } else {
                            // 不支持其它操作
                            responseData.setApiResult(JsonApiResult.FAILED_NOT_SUPPORT);
                        }
                    }

                }
            } catch (JsonProcessingException e) {
                // 负载数据格式错误
                responseData.setApiResult(JsonApiResult.FAILED_REQUEST_FORMAT_ERROR);
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
