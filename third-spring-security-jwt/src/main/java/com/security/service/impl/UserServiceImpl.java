package com.security.service.impl;

import com.security.constant.Messages;
import com.security.enums.CodeEnum;
import com.security.exception.BizException;
import com.security.mapper.RoleMapper;
import com.security.mapper.RoleUserMapper;
import com.security.mapper.UserMapper;
import com.security.model.LoginRegisterForm;
import com.security.model.SecurityUser;
import com.security.pojo.RoleUser;
import com.security.pojo.User;
import com.security.service.UserService;
import com.security.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleUserMapper roleUserMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void registerUser(LoginRegisterForm form) {
        User user = form.toUser(form);

        User exitedUser = userMapper.selectUserByUsername(user.getUsername());

        if (exitedUser != null) {
            throw new BizException(CodeEnum.SUCCESS.getCode(), Messages.ACCOUNT_HAS_EXIST);
        }

        userMapper.insertUser(user);

        RoleUser roleUser = new RoleUser();
        roleUser.setUserId(user.getId());
        roleUser.setRoleId(form.getRoleId());

        roleUserMapper.insertRoleUser(roleUser);
    }

    @Override
    public Map<String, Object> login(LoginRegisterForm form) {
        // ????????????
        Authentication authentication = null;
        try {
            // ?????????????????????????????????????????????????????????????????????????????????TokenAuthenticationFilter??????????????????????????????????????????????????????????????????????????????????????????
            // ?????????????????????????????????????????????????????????
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
            // ?????????????????????UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
            // ????????????Authentication??????????????????
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                throw new BizException(CodeEnum.USERNAME_PASSWORD_ERROR);
            } else {
                throw new BizException(e.getMessage());
            }
        }  finally {
            // ??????????????????????????????????????????
            SecurityContextHolder.clearContext();
        }

        Map<String, Object> token = new HashMap<>();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        // ??????token
        token.put("access-token", jwtUtils.createToken(securityUser));
        //???????????????????????????accessToken????????????????????????refreshToken?????????????????????refreshToken????????????????????????accessToken???
        token.put("refresh-token", jwtUtils.refreshToken(jwtUtils.createToken(securityUser)));

        return token;
    }

    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectUserByUsername(username);

        if (user == null) {
            //???????????????????????????UsernameNotFoundException???security???????????????BadCredentialsException
            throw new UsernameNotFoundException(username + "????????????");
        }

        SecurityUser securityUser = new SecurityUser();
        securityUser.setId(user.getId());
        securityUser.setUsername(user.getUsername());
        //todo ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        securityUser.setPassword(user.getPassword());
        List<RoleUser> roleUserList = roleUserMapper.selectRoleUserByUserId(user.getId());
        List<String> roleList = new ArrayList<>();
        String[] authoritiesArray = {};
        for (RoleUser roleUser : roleUserList) {
            roleList.add(roleMapper.selectRoleById(roleUser.getRoleId()).getRoleName());
        }
        //??????????????????
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roleList.toArray(authoritiesArray));
        securityUser.setAuthorities(authorities);

        return securityUser;
    }
}
