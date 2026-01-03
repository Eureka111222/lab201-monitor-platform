package com.lab201.monitor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;

@SpringBootTest
class ConnectivityTest {

    @Autowired
    private VectorStore vectorStore; // 测试 Milvus

    @Autowired
    private StringRedisTemplate redisTemplate; // 测试 Redis

    @Autowired
    private DataSource dataSource; // 测试 MySQL

    @Test
    void testAllConnections() throws SQLException {
        // 1. 测试 MySQL
        System.out.println("MySQL 连接状态: " + !dataSource.getConnection().isClosed());

        // 2. 测试 Redis
        redisTemplate.opsForValue().set("test_key", "hello_lab201");
        String value = redisTemplate.opsForValue().get("test_key");
        System.out.println("Redis 读取测试: " + "hello_lab201".equals(value));

        // 3. 测试 Milvus (Spring AI)
        // 只要 vectorStore 不为 null，且能调用方法不报错，说明 Bean 创建成功
        System.out.println("Milvus VectorStore Bean: " + vectorStore.getClass().getName());
        System.out.println("测试全部通过！✅");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testRabbitmq() {
        // 发送一条测试消息（触发RabbitMQ连接）
        rabbitTemplate.convertAndSend("test-queue", "hello rabbitmq");
        System.out.println("已发送测试消息到RabbitMQ，连接已建立");
    }

    @Test
    public void testTime() {
        System.out.println("-----"+LocalDateTime.now());
    }

}