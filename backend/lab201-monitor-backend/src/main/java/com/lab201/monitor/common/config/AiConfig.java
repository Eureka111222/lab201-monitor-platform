package com.lab201.monitor.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    //@Bean
    //public ChatClient chatClient(ChatClient.Builder builder) {
    //    // builder 会自动注入 application.yml 中配置的 Ollama 模型
    //    return builder
    //            .defaultSystem("你是一个专业的实验室AI助手。请根据提供的上下文知识回答问题。如果上下文没有相关信息，请诚实地回答不知道，不要编造。")
    //            .build();
    //}

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                    你是一个专业的Lab201实验室AI助手。
                    你的任务是根据用户的提问提供准确的回答。
                    请优先依据提供的【上下文知识】来回答。
                    如果【上下文知识】中不包含相关信息，请利用你的【通用知识】进行回答。
                    """)
                .build();
    }
}