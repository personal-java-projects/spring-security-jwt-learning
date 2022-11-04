package com.security.controller;

import cn.hutool.http.Method;
import com.security.utils.RestTemplateUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName() + Arrays.toString(authentication.getAuthorities().toArray());
    }

    @RequestMapping("/getCurrentUser")
    public Object getCurrentUser(Authentication authentication) {
        return authentication;
    }

    @GetMapping("/api/logout")
    public String authLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {//清除认证
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        // 认证中心退出请求
//        return "redirect:" + "http://localhost:8001/author/logout" + "?" + request.getQueryString();

        String result = RestTemplateUtils.getResponse("http://localhost:8001/author/logout", HttpMethod.GET, new HashMap<>(), new HashMap<>());

        return result;
    }
}
