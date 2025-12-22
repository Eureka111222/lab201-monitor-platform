package com.lab201.monitor.common.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.lab201.monitor.common.domain.BaseEntity; // 引入你的 BaseEntity

import java.sql.Types;
import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 数据库配置
        String url = "jdbc:mysql://192.168.31.130:3306/lab201_monitor?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "lab201201"; // 你的数据库密码

        // 2. 开始生成
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("lab201") // 设置作者
                            .enableSwagger() // 开启 swagger 模式 (可选)
                            .outputDir(System.getProperty("user.dir") + "/src/main/java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.lab201.monitor") // 设置父包名
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .xml("mapper.xml")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置 Mapper XML 输出目录
                })
                //.strategyConfig(builder -> {
                //    builder.addInclude("sys_user", "sys_role", "sys_user_role", "server_node", "gpu_metric_log", "sys_document") // 设置需要生成的表名
                //
                //            // === 实体类配置 ===
                //            .entityBuilder()
                //            .enableLombok() // 开启 Lombok
                //            .enableTableFieldAnnotation() // 开启字段注解
                //            .superClass(BaseEntity.class) // ✨ 核心：设置父类
                //            .addSuperEntityColumns("id", "create_time", "params") // ✨ 核心：这些字段在父类里，不要重复生成
                //            // 针对 server_node 表，开启自动 resultMap (为了 JSON 转换)
                //            //.enableAutoResultMap()
                //
                //            // === Controller 配置 ===
                //            .controllerBuilder()
                //            .enableRestStyle(); // 开启 @RestController
                //})
                .strategyConfig(builder -> {
                    builder.addInclude("sys_user", "sys_role", "sys_user_role", "server_node", "gpu_metric_log", "sys_document")
                            // 实体类配置：entityBuilder()的链式调用要连续
                            .entityBuilder()
                            .enableLombok() // 开启Lombok
                            .enableTableFieldAnnotation() // 开启字段注解
                            .superClass(BaseEntity.class) // 设置父类
                            .addSuperEntityColumns("id", "create_time", "params") // 父类字段
                            //.enableAutoResultMap() // ✅ 这里必须紧跟entityBuilder()，不能换行后断链
                            // Controller配置
                            .controllerBuilder()
                            .enableRestStyle(); // 开启RestController
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}