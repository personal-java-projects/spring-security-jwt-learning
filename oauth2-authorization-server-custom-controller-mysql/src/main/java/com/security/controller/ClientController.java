package com.security.controller;

import com.security.model.ClientDetailForm;
import com.security.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    /**
     * 注册oauth2的客户端
     * @return
     */
    @PostMapping("/registerClient")
    public ResponseEntity registerClient(@RequestBody ClientDetailForm clientDetailForm) throws Exception {

        clientService.addClient(clientDetailForm);

        return null;
    }
}
