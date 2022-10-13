package com.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.utils.BaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class ExtendAuthenticationEntryPointHandler extends OAuth2AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(ExtendAuthenticationEntryPointHandler.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, IOException {

        Throwable cause = authException.getCause();

        //自定义返回格式内容
        BaseResult baseResult = new BaseResult();
        baseResult.setSuccess(false);
        baseResult.setDetailMessage(authException.getMessage());
        baseResult.setMessage(authException.getMessage());

        if (cause instanceof OAuth2AccessDeniedException) {
            baseResult.setMessage("资源ID不在resource_ids范围内");
        }  else if (cause instanceof InvalidTokenException) {
            baseResult.setMessage("Token解析失败");
        }else if (authException instanceof InsufficientAuthenticationException) {
            baseResult.setMessage("未携带token");
        }else{
            baseResult.setMessage("未知异常信息");
        }

        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.append(new ObjectMapper().writeValueAsString(baseResult));

    }

}

