package com.lab201.monitor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "07. AI聊天管理")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Operation(summary = "测试ollama聊天功能")
    @GetMapping("/test")
    public String generate(@RequestParam(value = "message", defaultValue = "hello")
                           String message) {
        String response = this.ollamaChatModel.call(message);
        System.out.println("response : "+response);
        return response;
    }
}
