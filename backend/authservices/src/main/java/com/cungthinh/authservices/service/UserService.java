package com.cungthinh.authservices.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cungthinh.authservices.dto.request.UserCreationRequest;
import com.cungthinh.authservices.dto.request.UserUpdateRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.dto.response.RoleResponse;
import com.cungthinh.authservices.dto.response.UserResponse;
import com.cungthinh.authservices.entity.Role;
import com.cungthinh.authservices.entity.UserEntity;
import com.cungthinh.authservices.exception.CustomException;
import com.cungthinh.authservices.exception.ErrorCode;
import com.cungthinh.authservices.mapper.UserMapper;
import com.cungthinh.authservices.repository.RoleRepository;
import com.cungthinh.authservices.repository.UserResipotory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserResipotory userResipotory;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserMapper userMapper;

    public UserResponse addUser(UserCreationRequest userCreationRequest) {
        Optional<UserEntity> existUser = userResipotory.findByEmail(userCreationRequest.getEmail());
        if (existUser.isEmpty()) {
            HashSet<String> roles = new HashSet<String>();
            //            roles.add(Role.USER.toString());
            UserEntity newUser = UserEntity.builder()
                    .email(userCreationRequest.getEmail())
                    .password(passwordEncoder.encode(userCreationRequest.getPassword()))
                    .dob(userCreationRequest.getDob())
                    .build();
            userResipotory.save(newUser);
            return userMapper.toUserResponse(newUser);
        } else {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }

    public UserResponse me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userName = authentication.getName();

        log.info("Info: {}", authentication.getName());

        UserEntity user =
                userResipotory.findById(userName).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("#id == authentication.name || hasRole('ADMIN')")
    public ApiResponse<Object> getUserById(String id) {
        UserEntity user = userResipotory.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserResponse userResponse =
                UserResponse.builder().email(user.getEmail()).build();
        // UserResponse userResponse = UserResponse.builder().email(user.getEmail()).roles(user.getRoles()).build();
        return ApiResponse.success(userResponse, null);
    }

    public UserResponse updateUser(String id, UserUpdateRequest userUpdateRequest) {
        UserEntity user = userResipotory.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!userUpdateRequest.getRoleToAdd().isEmpty()) {
            Role role = roleRepository
                    .findById(userUpdateRequest.getRoleToAdd())
                    .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));
            user.getRoles().add(role);
            userResipotory.save(user);
        }
        Set<RoleResponse> roles = user.getRoles().stream()
                .map(r -> RoleResponse.builder()
                        .name(r.getName())
                        .description(r.getDescription())
                        .build())
                .collect(java.util.stream.Collectors.toSet());
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}
