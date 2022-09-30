package com.security.service;

import com.security.model.LoginRegisterForm;
import com.security.pojo.Role;
import com.security.pojo.User;

import java.util.List;

/**
 * 描述：UserService接口
 */
public interface UserService {
    Object getAllUsers();

    User getUserByUserName(String username);

    void save(LoginRegisterForm form);

    List<Role> getRolesByUserId(Integer userId);
}