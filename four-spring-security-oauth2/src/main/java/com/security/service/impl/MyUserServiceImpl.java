package com.security.service.impl;

import com.security.model.SecurityUser;
import com.security.pojo.User;
import com.security.service.MyUserService;
import com.security.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述：自定义UserDetailsService实现类
 * 名言：越难找的bug往往是越低级的
 */
@Service  //因为没有加Service注解，所以please login  一直报用户名密码错误！！！
public class MyUserServiceImpl implements UserDetailsService, MyUserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUserName(username);
        SecurityUser securityUser = new SecurityUser();
        securityUser.setId(user.getId());
        securityUser.setUsername(user.getUsername());
        securityUser.setPassword(user.getPassword());
        // 将List<Role>转为List<String>，再通过roleList.toArray()将List<String>转为String[]
        List<String> roleList = userService.getRolesByUserId(user.getId()).stream().map(role -> role.getRoleName()).collect(Collectors.toList());
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roleList.toArray(new String[roleList.size()]));
        securityUser.setAuthorities(authorities);

        return securityUser;
    }
}