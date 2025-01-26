package com.cungthinh.authservices.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.repository.UserResipotory;
import com.cungthinh.authservices.request.LoginRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserResipotory userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RedisTemplate<String, String> redisTemplate; // Tương tác với Redis để lưu session

    public Boolean login(LoginRequest loginRequest) {
        UserEntity user = userRepository
                .findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác"));

        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác");
        }

        return true;
    }
}
