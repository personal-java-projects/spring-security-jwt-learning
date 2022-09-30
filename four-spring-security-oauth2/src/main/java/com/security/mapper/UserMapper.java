package com.security.mapper;

import com.security.pojo.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    void insertUser(User user);

    void insertRoleUser(Integer roleId, Integer userId);

    User selectUserByUsername(String username);

    List<User> selectAllUsers();
}
