package com.security.controller;

import com.security.properties.RedisProperties;
import com.security.utils.Base64Util;
import com.security.utils.CookiesUtils;
import com.security.utils.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class HelloController {

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    private RedisProperties redisProperties;

    @GetMapping("/hello")
    public String hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName() + Arrays.toString(authentication.getAuthorities().toArray());
    }



    @GetMapping("/getSession")
    public Object getSession(HttpServletRequest request) {
//        sessionRepository.findById();
        Map<String, Cookie> cookies = CookiesUtils.getCookies(request);
        log.debug("cookies: " + cookies);
        log.debug("cookie: " + cookies.get(redisProperties.getCookieName()).getValue());
        log.debug("sessionId: " + Base64Util.decode(cookies.get(redisProperties.getCookieName()).getValue()));
        log.debug("session: " + sessionRepository.findById(Base64Util.decode(cookies.get(redisProperties.getCookieName()).getValue())).getMaxInactiveInterval().toMillis());
        return sessionRepository.findById(Base64Util.decode(cookies.get(redisProperties.getCookieName()).getValue()));
    }

    /**
     * 返回数据中的details对象中的tokenValue，是访问对应资源服务接口的 bearer token
     * @param authentication
     * @return
     */
    @RequestMapping("/getCurrentUser")
    public Object getCurrentUser(Authentication authentication){
        return authentication;
    }

    @RequestMapping("/toLogin")
    public ResponseResult login(Authentication authentication) {
        Map<String, Object> resultMap = new HashMap<>();
        Object details = authentication.getDetails();

        resultMap.put("details", details);

        return ResponseResult.builder().success(resultMap);
    }

    /**
     * 前端单点登录，走该接口，重定向回前端对应页面
     * @param response
     * @throws IOException
     */
    @RequestMapping("/toSuccess")
    public void success(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:8081/callback?state=ssoSuccess");
    }

    @RequestMapping("/toFail")
    public void fail(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:8081/callback?state=ssoFail");
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
