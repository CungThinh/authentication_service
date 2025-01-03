package com.cungthinh.authservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cungthinh.authservices.dto.UserDTO;
import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.repository.UserResipotory;
import com.cungthinh.authservices.resource.SuccessResource;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserResipotory userResipotory;

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userResipotory.findByEmail(username);
        UserDTO userData = UserDTO.builder().id(user.getId()).email(user.getEmail()).build();

        SuccessResource response = new SuccessResource("SUCCESS", userData);

        return ResponseEntity.ok(response);
    }
}
