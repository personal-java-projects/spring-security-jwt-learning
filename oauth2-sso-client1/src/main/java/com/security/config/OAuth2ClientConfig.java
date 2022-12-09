package com.security.config;

import com.security.properties.Oauth2Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @EnableOAuth2Client 和 @EnableOAuth2Sso 不能同时使用
 * @EnableOAuth2Sso 会自动走默认的 OAuth2ClientAuthenticationProcessingFilter
 * 下面的 OAuth2ClientAuthenticationProcessingFilter 不生效
 * 但 @EnableOAuth2Sso 目前我还没有解决登录成功后跳转到自定义的成功url
 */
@Configuration
@EnableOAuth2Client
public class OAuth2ClientConfig {

    @Autowired
    private Oauth2Properties oauth2Properties;

    OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails;

    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext context, OAuth2ProtectedResourceDetails details) {
        OAuth2RestTemplate template = new OAuth2RestTemplate(details, context);

        AuthorizationCodeAccessTokenProvider authCodeProvider = new AuthorizationCodeAccessTokenProvider();
        authCodeProvider.setStateMandatory(false);
        AccessTokenProviderChain provider = new AccessTokenProviderChain(
                Arrays.asList(authCodeProvider));
        template.setAccessTokenProvider(provider);

        return template;
    }

    /**
     * 注册处理redirect uri的filter
     * @param oauth2RestTemplate
     * @param tokenService
     * @return
     */
    @Bean
    public OAuth2ClientAuthenticationProcessingFilter oauth2ClientAuthenticationProcessingFilter(
            OAuth2RestTemplate oauth2RestTemplate,
            RemoteTokenServices tokenService) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter("/login");
        filter.setRestTemplate(oauth2RestTemplate);
        filter.setTokenServices(tokenService);


        //设置回调成功的页面
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler() {
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                this.setDefaultTargetUrl("/toSuccess");
                super.onAuthenticationSuccess(request, response, authentication);
            }
        });
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                this.setDefaultFailureUrl("/toFail");
                super.onAuthenticationFailure(request, response, exception);
            }
        });
        return filter;
    }

    /**
     * 设置 OAuth2ClientAuthenticationProcessingFilter 的优先级
     *
     * 解决 org.springframework.security.oauth2.client.resource.UserRedirectRequiredException: A redirect is required to get the users approval
     *
     * @param filter
     * @return
     */
    @Bean
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    /**
     * 注册check token服务
     * @param details
     * @return
     */
    @Bean
    public RemoteTokenServices tokenService(OAuth2ProtectedResourceDetails details) {
        RemoteTokenServices tokenService = new RemoteTokenServices();
        tokenService.setCheckTokenEndpointUrl("http://localhost:8001/author/oauth/check_token");
        tokenService.setClientId(details.getClientId());
        tokenService.setClientSecret(details.getClientSecret());
        return tokenService;
    }

    /**
	 * @功能描述: 授权客户端详情
	 * @版权信息：  www.easystudy.com
	 * @编写作者：  lixx2048@163.com
	 * @开发日期：  2020年8月1日
	 * @备注信息：
	 */
    @Bean
//    @ConfigurationProperties("security.oauth2.client")
    public AuthorizationCodeResourceDetails authorizationCodeResourceDetails() {
//    	return new AuthorizationCodeResourceDetails();
        AuthorizationCodeResourceDetails authorizationCodeResourceDetails = new AuthorizationCodeResourceDetails();
        authorizationCodeResourceDetails.setClientId(oauth2Properties.getClientId());
        authorizationCodeResourceDetails.setClientSecret(oauth2Properties.getClientSecret());
        authorizationCodeResourceDetails.setUserAuthorizationUri(oauth2Properties.getUserAuthorizationUri());
        authorizationCodeResourceDetails.setAccessTokenUri(oauth2Properties.getAccessTokenUri());
        authorizationCodeResourceDetails.setScope(oauth2Properties.getScopes());
        authorizationCodeResourceDetails.setUseCurrentUri(oauth2Properties.isUseCurrentUri());
        authorizationCodeResourceDetails.setPreEstablishedRedirectUri(oauth2Properties.getPreEstablishedRedirectUri());

        return authorizationCodeResourceDetails;
    }

    /**
     * @功能描述: 授权资源服务配置
     * @版权信息：  www.easystudy.com
     * @编写作者：  lixx2048@163.com
     * @开发日期：  2020年8月1日
     * @备注信息：
     */
    @Bean
//    @ConfigurationProperties("security.oauth2.resource")
    public ResourceServerProperties resourceServerProperties() {
        ResourceServerProperties resourceServerProperties = new ResourceServerProperties();
        ResourceServerProperties.Jwt jwt = resourceServerProperties.getJwt();

        resourceServerProperties.setUserInfoUri(oauth2Properties.getUserInfoUri());
        jwt.setKeyUri(oauth2Properties.getKeyUri());
        jwt.setKeyValue(oauth2Properties.getKeyValue());

//        return new ResourceServerProperties();
        return resourceServerProperties;
    }
}