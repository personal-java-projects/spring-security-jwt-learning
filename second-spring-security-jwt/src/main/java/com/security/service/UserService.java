package com.security.service;

import com.security.model.LoginRegisterForm;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface UserService extends UserDetailsService {

    void registerUser(LoginRegisterForm form);

    Map<String, Object> login(LoginRegisterForm form);

    void getRandomCode(HttpServletResponse response) throws IOException;
}
