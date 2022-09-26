package com.security.utils;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

/**
 * 掘金里无法导入整个redis工具类，我这里挑了几个需要的方法，仅供参考
 * redis工具类
 * @author yueranzs
 * @date 2021-03-04 10:08
 */
public class RedisUtil {
    //因为普通类无法直接使用RedisTemplate，这里用hutool中的SpringUtil来获取bean
    //如没引入hutool的可以百度下springboot中java普通类怎么调用mapper或service中的接口
    //关键注解@Component、@PostConstruct
    private static final RedisTemplate redisTemplate = SpringUtil.getBean("redisTemplate");

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public static boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }
}
