package com.cungthinh.authservices;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.repository.UserResipotory;
import com.redis.testcontainers.RedisContainer;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
@Testcontainers
class MaximumSessionsTests {

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

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserResipotory userResipotory;

    @BeforeEach
    void setUp() {
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        UserEntity user = UserEntity.builder()
                .email("macthin27@gmail.com")
                .password(passwordEncoder.encode("dontwastetime"))
                .roles(roles)
                .build();
        userResipotory.save(user);
    }

    @AfterEach()
    void after() {
        userResipotory.deleteAll();
    }

    @Test
    void loginOnSecondLoginThenFirstSessionTerminated() throws Exception {
        // Set max session to 1
        MvcResult mvcResult = this.mvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"macthin27@gmail.com\", \"password\":\"dontwastetime\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String sessionCookie = mvcResult.getResponse().getCookie("JSESSIONID").getValue();

        this.mvc
                .perform(get("/api/v1/auth/session-info").cookie(new Cookie("JSESSIONID", sessionCookie)))
                .andExpect(status().isOk());

        log.info("Second login");
        this.mvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"macthin27@gmail.com\", \"password\":\"dontwastetime\"}"))
                .andExpect(status().isOk());

        this.mvc
                .perform(get("/api/v1/auth/session-info").cookie(new Cookie("JSESSIONID", sessionCookie)))
                .andExpect(status().isForbidden()); // Session terminated
    }

    @Test
    void firstSessionInvalidWithThirdLogin() throws Exception {
        // set max session to 2
        MvcResult firstLoginResult = this.mvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"macthin27@gmail.com\", \"password\":\"dontwastetime\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie1 = firstLoginResult.getResponse().getCookie("JSESSIONID");
        assert cookie1 != null;

        this.mvc.perform(get("/api/v1/auth/session-info").cookie(cookie1)).andExpect(status().isOk());

        MvcResult secondLogin = this.mvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"macthin27@gmail.com\", \"password\":\"dontwastetime\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie2 = secondLogin.getResponse().getCookie("JSESSIONID");
        assert cookie2 != null;

        this.mvc.perform(get("/api/v1/auth/session-info").cookie(cookie2)).andExpect(status().isOk());

        this.mvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"macthin27@gmail.com\", \"password\":\"dontwastetime\"}"))
                .andExpect(status().isOk())
                .andReturn();

        this.mvc.perform(get("/api/v1/auth/session-info").cookie(cookie1)).andExpect(status().isForbidden());
    }
}
