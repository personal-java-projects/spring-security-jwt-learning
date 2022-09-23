package com.security.model;

import com.security.pojo.User;
import com.security.utils.PasswordUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class LoginRegisterForm {

    private String username;

    private String password;

    private Integer roleId;

    public User toUser(LoginRegisterForm form) {
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(PasswordUtil.passwordEncoder().encode(form.getPassword()));

        return user;
    }
}
