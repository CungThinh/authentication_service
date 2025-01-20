package com.cungthinh.authservices.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cungthinh.authservices.dto.request.UserCreationRequest;
import com.cungthinh.authservices.dto.request.UserUpdateRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.dto.response.UserResponse;
import com.cungthinh.authservices.service.UserService;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        UserResponse result = userService.addUser(userCreationRequest);
        ApiResponse<Object> response = ApiResponse.success(result, "Thêm user thành công");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Object> me() {
        UserResponse result = userService.me();
        ApiResponse<Object> response = ApiResponse.success(result, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        ApiResponse<Object> response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_DATA')")
    public ResponseEntity<Object> updateUser(
            @PathVariable String id, @RequestBody UserUpdateRequest userUpdateRequest) {
        UserResponse result = userService.updateUser(id, userUpdateRequest);
        ApiResponse<Object> apiResponse = ApiResponse.success(result, "Cập nhật user thành công");
        return ResponseEntity.ok(apiResponse);
    }
}
