package com.savt.listopia.service;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.model.RecaptchaResponse;
import com.savt.listopia.util.SpringEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    @Value("${recaptcha.secretKey}")
    private String secretKey;
    @Value("${recaptcha.verifyUrl}")
    private String verifyUrl;
    private final RestTemplate restTemplate;

    public CaptchaServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validateCaptcha(String recaptchaToken, String action, String remoteIp) {
        if (!SpringEnvironment.isProduction()) {
            System.out.println("Not production, recaptha captcha service is not available");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
        map.add("secret", secretKey);
        map.add("response", recaptchaToken);
        map.add("remoteip", remoteIp);
        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(map,headers);
        ResponseEntity<RecaptchaResponse> response = restTemplate.exchange(verifyUrl,
                HttpMethod.POST,
                entity,
                RecaptchaResponse.class);

        if (
                response.getBody() == null
                || !response.getBody().success()
                || response.getBody().score() == null
                || response.getBody().score() < 0.9
                || response.getBody().action() == null
                || !response.getBody().action().equalsIgnoreCase(action)
        ) {
            throw new APIException("recaptcha_invalid");
        }

    }
}
