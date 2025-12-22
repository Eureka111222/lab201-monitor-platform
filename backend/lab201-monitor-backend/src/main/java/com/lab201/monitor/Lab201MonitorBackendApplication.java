package com.lab201.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lab201.monitor.mapper")
public class Lab201MonitorBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Lab201MonitorBackendApplication.class, args);
    }

}
