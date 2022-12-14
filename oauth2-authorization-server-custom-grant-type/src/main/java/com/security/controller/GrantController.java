package com.security.controller;

import com.security.enums.CodeEnum;
import com.security.model.ClientDetailForm;
import com.security.model.LoginRegisterForm;
import com.security.model.SecurityUser;
import com.security.pojo.ClientDetail;
import com.security.service.ClientService;
import com.security.service.UserService;
import com.security.utils.RedisUtil;
import com.security.utils.ResponseResult;
import com.sun.deploy.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

@Controller
@SessionAttributes("authorizationRequest")
@RequestMapping("/auth")
@Slf4j
public class GrantController {

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;

    /**
     * ?????????????????????jwt?????????????????????TokenStore???????????????
     */
    @Qualifier("jwtTokenStore")
    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/register")
    @ResponseBody
    public ResponseResult registerUser(@RequestBody LoginRegisterForm form) {

        userService.registerUser(form);

        return ResponseResult.builder().code(CodeEnum.SUCCESS.getCode()).message(CodeEnum.SUCCESS.getMessage());
    }

    /**
     * ??????oauth2????????????
     * @return
     */
    @PostMapping("/registerClient")
    @ResponseBody
    public ResponseResult registerClient(@Valid @RequestBody ClientDetailForm clientDetailForm) throws Exception {

        ClientDetail clientDetail = clientDetailForm.toClientDetail(clientDetailForm);

        clientService.addClient(clientDetail);

        return null;
    }

    /**
     * ?????????????????????thymeleaf
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
        view.setViewName("/grant");
        view.addObject("clientId", authorizationRequest.getClientId());
        view.addObject("scopes", authorizationRequest.getScope());

        return view;
    }

    @GetMapping("/getCode")
    public ResponseEntity getCode(@RequestParam String code) {
        return ResponseEntity.ok(code);
    }

    @PostMapping("/token")
    @ResponseBody
    public ResponseResult postAccessToken(HttpServletRequest request, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        ClientDetails client;
        String clientId;

        // 1. ???????????????????????????
        String header = request.getHeader("Authorization");
        if (header == null || !header.toLowerCase().startsWith("basic ")) {
            clientId = parameters.get("client_id");
        } else {
            // ???????????????
            String[] clients = extractAndDecodeHeader(header);
            if (clients.length != 2) {
                throw new BadCredentialsException("Invalid basic authentication token");
            }

            clientId = clients[0];
        }

        // ?????????????????????????????????
        client = clientDetailsService.loadClientByClientId(clientId);

        // ???ClientDetails??????????????????
        SecurityUser securityUser = new SecurityUser();
        securityUser.setUsername(client.getClientId());
        securityUser.setPassword(client.getClientSecret());
        securityUser.setAuthorities(new ArrayList<>());

        // ????????????????????????UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(securityUser, null, new ArrayList<>());

        // ???????????????????????????parameters???????????????
        OAuth2AccessToken accessToken = tokenEndpoint.postAccessToken(usernamePasswordAuthenticationToken, parameters).getBody();

        Map<String, Object> resultMap = new HashMap<>();

        // token??????
        resultMap.put("access_token", accessToken.getValue());
        resultMap.put("refresh_token", accessToken.getRefreshToken());
        resultMap.put("token_type", accessToken.getTokenType());
        resultMap.put("expires_in", accessToken.getExpiresIn());
        resultMap.put("scope", StringUtils.join(accessToken.getScope(), ","));
        resultMap.putAll(accessToken.getAdditionalInformation());

        OAuth2Authentication authentication = tokenStore.readAuthentication(accessToken);
        Authentication userAuthentication = authentication.getUserAuthentication();
        Collection<? extends GrantedAuthority> authorities = userAuthentication.getAuthorities();
        // ????????????
        List<String> list = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            list.add(authority.getAuthority());
        }

        resultMap.put("authorities", list);


        return ResponseResult.builder().code(CodeEnum.SUCCESS.getCode()).message(CodeEnum.SUCCESS.getMessage()).data(resultMap);
    }

    @RequestMapping("/loginFail")
    public ModelAndView loginFail() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/login-fail");
        modelAndView.addObject("message", "????????????????????????");
        return modelAndView;
    }

    @PostMapping(value = "/login")
    public ResponseResult login(@RequestBody LoginRegisterForm req) throws HttpRequestMethodNotSupportedException {
        return null;
    }

    @GetMapping("/getCaptcha")
    public void getRandomCode(HttpServletResponse response) throws IOException {
        userService.getRandomCode(response);
    }

    @GetMapping("/sendCode")
    @ResponseBody
    public ResponseResult sendCode(String phone) {
        // 1. ??????????????????
        log.info(phone + "?????????????????????");
        // 2. ?????????????????????????????????????????????????????????KEY???????????????????????????Redis????????????????????????
        String code = generateRandomCode(6);
        redisUtil.set(phone, code, 60*10);
        String saveCode = (String) redisUtil.get(phone);// ????????????code
        Long expire = redisUtil.getExpire(phone); // ??????????????????
        // 3. ???????????????????????????????????????????????????????????????
        Map<String,String> result=new HashMap<>();
        result.put("code",saveCode);
        result.put("????????????",expire+"???");
        return ResponseResult.builder().code(CodeEnum.SUCCESS.getCode()).message(CodeEnum.SUCCESS.getMessage()).data(result);
    }

    private String generateRandomCode(int len) {
        Random random = new Random();
        String result="";
        for (int i=0;i<len;i++)
        {
            result+=random.nextInt(10);
        }

        return result;
    }

    /**
     * ????????????????????????????????????
     *
     * @param header ?????????
     * @return ???????????????
     */
    private String[] extractAndDecodeHeader(String header) {
        byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(
                    "Failed to decode basic authentication token");
        }
        String token = new String(decoded, StandardCharsets.UTF_8);
        int delimiter = token.indexOf(":");

        if (delimiter == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return new String[]{token.substring(0, delimiter), token.substring(delimiter + 1)};
    }

}
