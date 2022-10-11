package com.security.controller;

import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@SessionAttributes("authorizationRequest")
@RequestMapping("/auth")
public class GrantController {

    /**
     * 自定义授权页面thymeleaf
     *
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/confirm_access")
    public ModelAndView getAccessConfirmation(Map<String, Object> model, HttpServletRequest request) throws Exception {
        AuthorizationRequest authorizationRequest = (AuthorizationRequest) model.get("authorizationRequest");
        ModelAndView view = new ModelAndView();
//        view.setViewName("/html/grant.html"); // 没使用thymeleaf，且未配置视图解析器，在这里需要使用完整路径
        view.setViewName("/grant"); // 使用thymeleaf，grant前面不能少了`/`，且已经在application.yml中配置了thymeleaf的视图解析器
        view.addObject("clientId", authorizationRequest.getClientId());
        view.addObject("scopes", authorizationRequest.getScope());
        return view;
    }

    /**
     * base-login.html由模板转成springboot能识别的静态资源
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/base-login")
    public ModelAndView login(Map<String, Object> model, HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("/base-login");

        return modelAndView;
    }
}
