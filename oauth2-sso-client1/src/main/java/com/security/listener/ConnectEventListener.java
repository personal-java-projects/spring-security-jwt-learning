package com.security.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

/**
 * 连接事件监听器
 * @author: chenyanbin 2022-07-03 17:35
 */
@Component
public class ConnectEventListener implements ApplicationListener<SessionConnectEvent> {

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        Message<byte[]> message = event.getMessage();
        //消息头响应器
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        //消息类型
        SimpMessageType messageType = headerAccessor.getCommand().getMessageType();
        System.err.println("【ConnectEventListener监听器事件】消息类型："+messageType);
    }
}
