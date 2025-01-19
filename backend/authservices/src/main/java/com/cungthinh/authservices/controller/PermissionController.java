package com.cungthinh.authservices.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cungthinh.authservices.dto.request.PermissionRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.dto.response.PermissionResponse;
import com.cungthinh.authservices.service.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("api/v1/permission")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {

    private PermissionService permissionService;

    @PostMapping
    public ResponseEntity<?> createPermission(@RequestBody PermissionRequest permissionRequest) {
        PermissionResponse result = permissionService.create(permissionRequest);
        ApiResponse<Object> apiResponse = ApiResponse.success(result, "Tạo permission thành công");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<?> getAllPermissions() {
        List<PermissionResponse> result = permissionService.getAll();
        ApiResponse<Object> apiResponse = ApiResponse.success(result, null);
        return ResponseEntity.ok(apiResponse);
    }
}
