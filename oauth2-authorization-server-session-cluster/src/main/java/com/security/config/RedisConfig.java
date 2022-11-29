package com.security.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

@Configuration
@EnableCaching
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400, redisNamespace = "spring:clusterSession")
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }


    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();

        // 构建白名单，解决SecurityUser中的Collection类型无法序列化和反序列化的问题
        BasicPolymorphicTypeValidator basicPolymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Collection.class)
                .allowIfSubType(Number.class)
                .allowIfSubType(Map.class)
                .allowIfSubType(Temporal.class)
                .allowIfSubType(Object.class)
                .allowIfSubTypeIsArray()
                .build();
        objectMapper.activateDefaultTyping(basicPolymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 解决SecurityUser中没有Set方法导致的序列化失败的错误
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /**
         * 解决如下问题：
         * org.springframework.data.redis.serializer.SerializationException:
         * Could not read JSON: Cannot construct instance of `org.springframework.security.web.savedrequest.DefaultSavedRequest`
         * (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
         */
        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 设置键（key）的序列化采用StringRedisSerializer。
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    /**
     * 必须在这里进行session进行redis的序列化配置
     *
     * @return
     */
    @Bean
    public RedisSerializer<?> springSessionDefaultRedisSerializer(RedisTemplate<?, ?> redisTemplate) {
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
//
//        ObjectMapper objectMapper = new ObjectMapper();
////        objectMapper.registerModule(new CoreJackson2Module());
////        objectMapper.registerModule(new WebJackson2Module());
////        objectMapper.registerModule(new WebServerJackson2Module());
////        objectMapper.registerModule(new WebServletJackson2Module());
////        objectMapper.registerModule(new OAuth2ClientJackson2Module());
//
//        // 构建一个基本的白名单
//        BasicPolymorphicTypeValidator basicPolymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
//                .allowIfSubType(Collection.class)
//                .allowIfSubType(Number.class)
//                .allowIfSubType(Map.class)
//                .allowIfSubType(Temporal.class)
//                .allowIfSubType(Object.class)
//                .allowIfSubTypeIsArray()
//                .build();
//        objectMapper.activateDefaultTyping(basicPolymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//
//        // 解决SecurityUser中没有Set方法导致的序列化失败的错误
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//        /**
//         * 解决如下问题：
//         * org.springframework.data.redis.serializer.SerializationException:
//         * Could not read JSON: Cannot construct instance of `org.springframework.security.web.savedrequest.DefaultSavedRequest`
//         * (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
//         */
//        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
//
//        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
//
//        return jackson2JsonRedisSerializer;
        return redisTemplate.getValueSerializer();
    }

    //    /**
//     * 设置spring session redis 序列化方式
//     * 这种序列化配置方式，返回值必须是RedisTemplate<Object, Object>
//     * @param factory
//     * @return
//     */
//    @Bean
//    public SessionRepository sessionRepository(RedisConnectionFactory redisConnectionFactory) {
//        RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(redisTemplate(redisConnectionFactory));
//        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
//        sessionRepository.setDefaultSerializer(fastJsonRedisSerializer);
//        sessionRepository.setDefaultMaxInactiveInterval(36000);
//        return sessionRepository;
//    }
}