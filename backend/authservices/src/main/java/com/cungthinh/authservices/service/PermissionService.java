package com.cungthinh.authservices.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cungthinh.authservices.dto.request.PermissionRequest;
import com.cungthinh.authservices.dto.response.PermissionResponse;
import com.cungthinh.authservices.entity.Permission;
import com.cungthinh.authservices.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;

    public PermissionResponse create(PermissionRequest permissionRequest) {
        Permission permission = new Permission(permissionRequest.getName(), permissionRequest.getDescription());
        log.info("Saving Permission: {}", permissionRequest);
        PermissionResponse permissionResponse = PermissionResponse.builder()
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
        permissionRepository.save(permission);
        return permissionResponse;
    }

    public List<PermissionResponse> getAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(permission -> PermissionResponse.builder()
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .build())
                .toList();
    }
}
