package com.lab201.monitor.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.springframework.ai.chat.client.ChatClient.builder;

@Configuration
public class AiConfig {

    //@Bean
    //public ChatClient chatClient(ChatClient.Builder builder) {
    //    // builder 会自动注入 application.yml 中配置的 Ollama 模型
    //    return builder
    //            .defaultSystem("你是一个专业的实验室AI助手。请根据提供的上下文知识回答问题。如果上下文没有相关信息，请诚实地回答不知道，不要编造。")
    //            .build();
    //}

    //@Bean
    //public ChatClient chatClient(ChatClient.Builder builder) {
    //    return builder
    //            .defaultSystem("""
    //                你是一个专业的Lab201实验室AI助手。
    //                你的任务是根据用户的提问提供准确的回答。
    //                请优先依据提供的【上下文知识】来回答。
    //                如果【上下文知识】中不包含相关信息，请利用你的【通用知识】进行回答。
    //                """)
    //            .build();
    //}

    //@Bean("deepseekChatClient") // 给 Bean 起个名字
    //public ChatClient deepseekChatClient(ChatClient.Builder builder, OpenAiChatModel openAiChatModel) {
    //    return builder(openAiChatModel)// 🔥 关键：显式指定使用 OpenAI (DeepSeek) 模型
    //            .defaultSystem("""
    //                你是一个专业的Lab201实验室AI助手（云端版）。
    //                你的任务是根据用户的提问提供准确的回答。
    //                请优先依据提供的【上下文知识】来回答。
    //                """)
    //            .build();
    //}

    @Primary()
    @Bean("ollamaChatClient") // 给 Bean 起个名字
    public ChatClient ollamaChatClient(ChatClient.Builder builder, OllamaChatModel ollamaChatModel) {
        return builder(ollamaChatModel)// 🔥 关键：显式指定使用 Ollama 模型
                .defaultSystem("""
                    你是一个专业的Lab201实验室AI助手（本地版）。
                    你的任务是根据用户的提问提供准确的回答。
                    请优先依据提供的【上下文知识】来回答。
                    """)
                .build();
    }

    //@Bean
    //@Primary
    //public OllamaEmbeddingModel primaryEmbeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {
    //    return ollamaEmbeddingModel;
    //}

}