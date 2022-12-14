package com.security.config;

import com.security.handler.CustomAuthenticationFailureHandler;
import com.security.handler.CustomSessionAuthenticationStrategy;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @EnableOAuth2Client ??? @EnableOAuth2Sso ??????????????????
 * @EnableOAuth2Sso ????????????????????? OAuth2ClientAuthenticationProcessingFilter
 * ????????? OAuth2ClientAuthenticationProcessingFilter ?????????
 * ??? @EnableOAuth2Sso ??????????????????????????????????????????????????????????????????url
 *
 */
@Configuration
@EnableOAuth2Client
public class OAuth2ClientConfig {

    /**
     * @Feild: ???????????????????????????????????????
     */
    private final String SUCCESS_FORWARD_URL = "/toSuccess";

    /**
     * @Feild: ???????????????????????????????????????
     */
    private final String FAIL_FORWARD_URL = "/toFail";

    @Autowired
    private Oauth2Properties oauth2Properties;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private CustomSessionAuthenticationStrategy customSessionAuthenticationStrategy;

    /**
     * ?????? OAuth2ClientAuthenticationProcessingFilter ????????????
     *
     * ?????? org.springframework.security.oauth2.client.resource.UserRedirectRequiredException: A redirect is required to get the users approval
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
     * ????????????redirect uri???filter
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

        filter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
        filter.setSessionAuthenticationStrategy(customSessionAuthenticationStrategy);

        //???????????????????????????
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler() {
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                this.setDefaultTargetUrl(SUCCESS_FORWARD_URL);
                super.onAuthenticationSuccess(request, response, authentication);
            }
        });
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                this.setDefaultFailureUrl(FAIL_FORWARD_URL);
                super.onAuthenticationFailure(request, response, exception);
            }
        });

        return filter;
    }

    /**
     * ??????check token??????
     * @param details
     * @return
     */
    @Bean
    public RemoteTokenServices tokenService(OAuth2ProtectedResourceDetails details) {
        RemoteTokenServices tokenService = new RemoteTokenServices();
        tokenService.setCheckTokenEndpointUrl(oauth2Properties.getCheckTokenUri());
        tokenService.setClientId(details.getClientId());
        tokenService.setClientSecret(details.getClientSecret());

        return tokenService;
    }

    /**
	 * @????????????: ?????????????????????
	 * @???????????????  www.easystudy.com
	 * @???????????????  lixx2048@163.com
	 * @???????????????  2020???8???1???
	 * @???????????????
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
     * @????????????: ????????????????????????
     * @???????????????  www.easystudy.com
     * @???????????????  lixx2048@163.com
     * @???????????????  2020???8???1???
     * @??????????????????????????????????????????????????????????????????????????????????????????
     */
    @Bean
//    @ConfigurationProperties("security.oauth2.resource")
    public ResourceServerProperties resourceServerProperties() {
//        return new ResourceServerProperties();

        ResourceServerProperties resourceServerProperties = new ResourceServerProperties();
        ResourceServerProperties.Jwt jwt = resourceServerProperties.getJwt();

        resourceServerProperties.setUserInfoUri(oauth2Properties.getUserInfoUri());
        jwt.setKeyUri(oauth2Properties.getKeyUri());
        jwt.setKeyValue(oauth2Properties.getKeyValue());

        return resourceServerProperties;
    }
}