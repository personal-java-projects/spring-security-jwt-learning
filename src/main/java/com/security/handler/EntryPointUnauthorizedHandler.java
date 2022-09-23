package com.security.handler;

import com.security.constant.Messages;
import com.security.enums.BaseErrorInfo;
import com.security.enums.CodeEnum;
import com.security.utils.ResponseResult;
import com.security.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户访问受保护的资源，但是用户没有通过认证则会进入这个处理器
 * 返回json数据，阻止重定向
 */
@Component
@Slf4j
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ResponseUtils.result(response,new ResponseResult(CodeEnum.FORBIDDEN.getCode(), CodeEnum.FORBIDDEN.getMessage()));
    }
}
