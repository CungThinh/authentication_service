package com.cungthinh.authservices.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.cungthinh.authservices.dto.UserCreationRequest;
import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.repository.UserResipotory;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserResipotory userResipotory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        String username = loggedInUser.getPrincipal().toString();
        return ResponseEntity.ok().body(username);
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody UserCreationRequest userCreationRequest) {
        UserEntity user = new UserEntity();
        user.setEmail(userCreationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));
        userResipotory.save(user);

        return ResponseEntity.status(201).body("Tạo user thành công");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> admin() {
        return ResponseEntity.ok().body("Admin");
    }
}
