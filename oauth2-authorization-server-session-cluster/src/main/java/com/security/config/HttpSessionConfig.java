package com.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Slf4j
@Configuration
//  60 * 60 * 24 为 24小时
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60, redisNamespace = "spring:clusterSession")
public class HttpSessionConfig {

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Bean
    public SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
}
