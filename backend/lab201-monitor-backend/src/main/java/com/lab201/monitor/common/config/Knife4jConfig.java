package com.lab201.monitor.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lab201 监控平台接口文档")
                        .version("1.0")
                        .description("这是 Lab201 监控平台的后端 API 接口文档")
                        .contact(new Contact().name("yuzhaoli").email("yuzhaolics@163.com")));
    }
}