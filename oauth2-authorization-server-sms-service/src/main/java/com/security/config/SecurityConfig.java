package com.security.config;

import com.security.filter.ValidateCodeFilter;
import com.security.granter.SmsAuthenticationProvider;
import com.security.granter.SmsAuthenticationSecurityConfig;
import com.security.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.annotation.Resource;
import java.util.Arrays;

@Configuration
public class SecurityConfig  extends WebSecurityConfigurerAdapter {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ValidateCodeFilter validateCodeFilter;

    @Resource
    private SmsAuthenticationSecurityConfig smsAuthenticationSecurityConfig;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .apply(smsAuthenticationSecurityConfig)
                .and()
                .formLogin()
                .loginPage("/base-login.html") //???????????????????????? **??????**
                .loginProcessingUrl("/login")
                // ????????????security???????????????????????????
                .failureForwardUrl("/auth/loginFail")
                .and()
                .authorizeRequests()
                // /oauth/**?????????????????????????????????????????????????????????????????????????????????????????????????????????
                .antMatchers("/login", "/base-login.html", "/auth/**", "/oauth/sms", "/code/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutSuccessUrl("/auth/logoutSuccess")
                .deleteCookies("JSESSIONID")
                .and()
                .sessionManagement()
                .sessionCreationPolicy( SessionCreationPolicy.IF_REQUIRED )
                .and()
                .csrf().disable();

        // ????????????????????????
        http
                .addFilterBefore(validateCodeFilter, AbstractPreAuthenticatedProcessingFilter.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
