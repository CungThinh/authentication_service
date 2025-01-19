package com.cungthinh.authservices.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.cungthinh.authservices.entity.UserEntity;
import com.cungthinh.authservices.repository.UserResipotory;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@Testcontainers
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private UserEntity user;

    @Autowired
    private UserResipotory userResipotory;

    private UserEntity savedUser;

    @BeforeEach
    void initData() {
        UserEntity testUser = UserEntity.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        savedUser = userResipotory.saveAndFlush(testUser);
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @Test
    @WithMockUser(
            username = "test-uuid-123",
            roles = {"USER"})
    void getMyInfo_validRequest_success() throws Exception {
        // Now use @WithMockUser with the generated ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/me")
                        .with(user(savedUser.getId()).roles("USER")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
