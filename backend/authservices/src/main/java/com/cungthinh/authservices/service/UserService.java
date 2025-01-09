package com.cungthinh.authservices.service;

import java.security.Security;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cungthinh.authservices.dto.request.UserCreationRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.enums.Role;
import com.cungthinh.authservices.exception.CustomException;
import com.cungthinh.authservices.exception.ErrorCode;
import com.cungthinh.authservices.repository.UserResipotory;

import lombok.extern.slf4j.Slf4j;

import com.cungthinh.authservices.dto.response.UserResponse;

@Service
@Slf4j
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserResipotory userResipotory;

    public ApiResponse<Object> addUser(UserCreationRequest userCreationRequest) {
        Optional<UserEntity> existUser = userResipotory.findByEmail(userCreationRequest.getEmail());
        if (!existUser.isPresent()) {
            HashSet<String> roles = new HashSet<String>();
            roles.add(Role.USER.toString());
            UserEntity newUser = UserEntity.builder().email(userCreationRequest.getEmail())
                    .password(passwordEncoder.encode(userCreationRequest.getPassword())).roles(roles).build();
            userResipotory.save(newUser);
            return ApiResponse.success(null, "User created successfully");
        } else {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }

    public ApiResponse<Object> me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userName = authentication.getName();

        log.info("Info: {}", authentication.getName());

        UserEntity user = userResipotory.findById(userName)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserResponse userResponse = UserResponse.builder().email(user.getEmail()).roles(user.getRoles()).build();
        return ApiResponse.success(userResponse, null);
    }

    @PreAuthorize("#id == authentication.name || hasRole('ADMIN')")
    public ApiResponse<Object> getUserById(String id) {
        UserEntity user = userResipotory.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserResponse userResponse = UserResponse.builder().email(user.getEmail()).roles(user.getRoles()).build();
        return ApiResponse.success(userResponse, null);
    }
}
