package com.security.controller;

import com.security.model.InMessage;
import com.security.model.OutMessage;
import com.security.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class GameInfoController {

    @Autowired
    private WebSocketService webSocketService;

    //消息路由，别人发送的消息会到这里来
    @MessageMapping("/v1/chat")
    //发送哪里去
    @SendTo("/topic/game_chat")
    public OutMessage gameInfo(InMessage message) {
        log.info("接收信息：{}", message);
        OutMessage outMessage = new OutMessage();
        BeanUtils.copyProperties(message, outMessage);
        return outMessage;
    }

    //消息路由，别人发送的消息会到这里来
    @MessageMapping("/v2/chat")
    public void chat(InMessage message) {
        log.info("接收信息：{}", message);
        webSocketService.sendTopicMessage(message);
    }

    @MessageMapping("/v3/single/chat")
    public void singleChat(InMessage message) {
        webSocketService.sendChatMessage(message);
    }
}
