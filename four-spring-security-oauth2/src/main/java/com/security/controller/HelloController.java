package com.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/admin/hello")
    public String admin(){
        return "Hello admin!";
    }

    @GetMapping("user/hello")
    public String user(){
        return "user admin!";
    }

    @GetMapping("hello")
    public String hello(){
        return "Hello!";
    }
}