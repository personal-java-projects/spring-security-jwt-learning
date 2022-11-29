package com.security.controller;

import cn.hutool.http.Method;
import com.security.utils.CookiesUtils;
import com.security.utils.RestTemplateUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName() + Arrays.toString(authentication.getAuthorities().toArray());
    }

    @RequestMapping("/getCurrentUser")
    public Object getCurrentUser(Authentication authentication){
        return authentication;
    }

    /**
     * 前端单点登录，走该接口，重定向回前端对应页面
     * @param response
     * @throws IOException
     */
    @RequestMapping("/toLogin")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:8080/callback?state=sso");
    }

    @GetMapping("/api/logout")
    public HttpEntity authLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {//清除认证
            new SecurityContextLogoutHandler().logout(request, response, auth);
            // 因为我使用的单点登录认证方式是默认的session方式。
            // 这边删除客户端的cookie,客户端才算退出登录。如果需要认证服务器，只需要删除认证服务器的cookie
//            CookiesUtils.set(response, "client1", null, 0);
        }

        // 认证中心退出请求
//        return "redirect:" + "http://localhost:8001/author/logout" + "?" + request.getQueryString();
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put("cookieName", "client1");
//
//        String result = RestTemplateUtils.getResponse("http://localhost:8001/author/logout", HttpMethod.GET, urlParams, new HashMap<>());

        return ResponseEntity.ok("退出成功");
    }
}
