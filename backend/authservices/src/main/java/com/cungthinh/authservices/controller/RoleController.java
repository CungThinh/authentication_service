package com.cungthinh.authservices.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cungthinh.authservices.dto.request.RoleRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.dto.response.RoleResponse;
import com.cungthinh.authservices.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("api/v1/role")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PostMapping
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse result = roleService.create(roleRequest);
        ApiResponse<Object> apiResponse = ApiResponse.success(result, "Tạo role thành công");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        ApiResponse<Object> apiResponse = ApiResponse.success(roleService.getAll(), null);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{id}/add-permission")
    public ResponseEntity<?> addPermissionToRole(@PathVariable String id, @RequestBody String permissionName) {
        roleService.addPermissionToRole(id, permissionName);
        ApiResponse<Object> apiResponse = ApiResponse.success(null, "Thêm permission vào role thành công");
        return ResponseEntity.ok(apiResponse);
    }
}
