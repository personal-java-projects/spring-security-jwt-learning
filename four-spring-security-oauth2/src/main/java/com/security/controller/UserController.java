package com.security.controller;

import com.security.utils.ResponseResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RequestMapping("/user")
public class UserController {

    @GetMapping("/getUserInfo")
    public ResponseResult getUserInfo(Authentication authentication, HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        // bearer :jwt token,所以bearer加空格后的第七个才是token。
        String token = header.substring(header.lastIndexOf("bearer") + 7);
        Claims body = Jwts.parser()
                .setSigningKey("test_key".getBytes(StandardCharsets.UTF_8))//指定编码格式，要不然token有中文转换异常
                .parseClaimsJws(token)
                .getBody();
        return null;
    }
}
