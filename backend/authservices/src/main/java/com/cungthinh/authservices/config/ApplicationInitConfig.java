package com.cungthinh.authservices.config;

import java.util.HashSet;

import org.hibernate.engine.jdbc.env.internal.LobCreationLogging_.logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.enums.Role;
import com.cungthinh.authservices.repository.UserResipotory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfig {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserResipotory userResipotory) {
        return args -> {
            if (!userResipotory.findByEmail("admin@gmail.com").isPresent()) {
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.toString());
                UserEntity user = UserEntity.builder()
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("dontwastetime"))
                        .roles(roles)
                        .build();

                userResipotory.save(user);
                log.warn("Tài khoản admin đã được tạo mặc định");
            }
        };
    }

}
