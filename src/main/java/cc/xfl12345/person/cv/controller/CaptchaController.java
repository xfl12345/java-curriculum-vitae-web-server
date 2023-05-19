package cc.xfl12345.person.cv.controller;


import cc.xfl12345.person.cv.model.SmsService;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.request.SmsValidationCodeRequestData;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


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
        HttpSession httpSession = request.getSession();
        synchronized (httpSession) {

        }


        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        boolean currentResult = imageCaptchaApplication.matching(id, imageCaptchaTrack);

        if (currentResult) {
            try {
                SmsValidationCodeRequestData extraData = objectMapper.treeToValue(objectMapper.valueToTree(imageCaptchaTrack.getData()), SmsValidationCodeRequestData.class);
                String phoneNumber = extraData.data != null && extraData.data.phoneNumber != null ? extraData.data.phoneNumber : "";
                String operation = extraData.operation != null ? extraData.operation : "";
                // 请求拉取短信验证码
                if (operation.equals("pull-sms-validation-code") && !"".equals(phoneNumber)) {
                    currentResult = smsService.sendSmsValidationCode(phoneNumber);
                    responseData.setApiResult(currentResult ? JsonApiResult.SUCCEED : JsonApiResult.FAILED);
                } else {
                    currentResult = false;
                    responseData.setApiResult(JsonApiResult.FAILED_INVALID);
                }
            } catch (JsonProcessingException e) {
                currentResult = false;
                responseData.setApiResult(JsonApiResult.FAILED_REQUEST_FORMAT_ERROR);
            }
        } else {
            responseData.setApiResult(JsonApiResult.FAILED_FORBIDDEN);
        }

        responseData.setData(Map.of("result", currentResult));
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