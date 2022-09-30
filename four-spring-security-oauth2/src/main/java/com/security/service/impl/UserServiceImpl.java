package com.security.service.impl;

import com.security.mapper.RoleMapper;
import com.security.mapper.RoleUserMapper;
import com.security.mapper.UserMapper;
import com.security.model.LoginRegisterForm;
import com.security.pojo.Role;
import com.security.pojo.RoleUser;
import com.security.pojo.User;
import com.security.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：UserServiceImpl
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleUserMapper roleUserMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public Object getAllUsers() {
        List<User> users = userMapper.selectAllUsers();
        return users;
    }

    @Override
    public User getUserByUserName(String username) {
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public void save(LoginRegisterForm form) {
        userMapper.insertUser(form.toUser(form));
    }

    @Override
    public List<Role> getRolesByUserId(Integer userId) {
        List<RoleUser> roleUsers = roleUserMapper.selectRoleUserByUserId(userId);
        List<Role> roleList = new ArrayList<>();

        for (RoleUser roleUser:roleUsers) {
           Role role = roleMapper.selectRoleById(roleUser.getRoleId());

           roleList.add(role);
        }

        return roleList;
    }

}