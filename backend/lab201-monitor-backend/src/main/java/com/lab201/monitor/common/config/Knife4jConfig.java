//package com.lab201.monitor.common.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Contact;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.info.License;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class Knife4jConfig {
//
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Lab201 监控平台 API 接口文档")
//                        .version("1.0")
//                        .description("基于 Spring Boot 3.4 + Knife4j 4.5 的接口文档")
//                        .termsOfService("http://lab201.com")
//                        .contact(new Contact()
//                                .name("Lab201 Admin")
//                                .url("http://lab201.com")
//                                .email("admin@lab201.com"))
//                        .license(new License()
//                                .name("Apache 2.0")
//                                .url("http://springdoc.org")));
//    }
//}