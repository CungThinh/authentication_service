package com.cungthinh.authservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cungthinh.authservices.dto.request.UserCreationRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        ApiResponse<Object> response = userService.addUser(userCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Object> me() {
        ApiResponse<Object> response = userService.me();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        ApiResponse<Object> response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}
