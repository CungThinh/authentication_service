package com.cungthinh.authservices;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;

@SpringBootTest
@Testcontainers
class AuthservicesApplicationTests {
    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("authservices")
            .withUsername("postgres")
            .withPassword("dontwastetime");

    @Container
    private static final RedisContainer redis =
            new RedisContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        // primary
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void contextLoads() {
        System.out.println("PostgreSQL JDBC URL: " + postgreSQLContainer.getJdbcUrl());
        System.out.println("Redis Host: " + redis.getHost());
        System.out.println("Redis Port: " + redis.getMappedPort(6379));
    }
}
