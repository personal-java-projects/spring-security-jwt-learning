package com.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.utils.BaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ExtendAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(ExtendAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        // 自定义返回格式内容
        BaseResult baseResult = new BaseResult();
        baseResult.setSuccess(false);
        baseResult.setMessage("认证过的用户访问无权限资源时的异常");
        baseResult.setDetailMessage(accessDeniedException.getMessage());

        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());// 权限不足403
        response.getWriter().write(new ObjectMapper().writeValueAsString(baseResult));

    }
}

