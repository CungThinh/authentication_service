package com.cungthinh.authservices.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cungthinh.authservices.dto.request.RoleRequest;
import com.cungthinh.authservices.dto.response.PermissionResponse;
import com.cungthinh.authservices.dto.response.RoleResponse;
import com.cungthinh.authservices.entity.Permission;
import com.cungthinh.authservices.entity.Role;
import com.cungthinh.authservices.exception.CustomException;
import com.cungthinh.authservices.exception.ErrorCode;
import com.cungthinh.authservices.repository.PermissionRepository;
import com.cungthinh.authservices.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    public RoleResponse create(RoleRequest roleRequest) {
        Set<Permission> permissions = roleRequest.getPermissions().stream()
                .map(permission -> permissionRepository
                        .findById(permission)
                        .orElseThrow(() -> new CustomException(ErrorCode.PERMISSION_NOT_FOUND)))
                .collect(Collectors.toSet());

        Role role = Role.builder()
                .name(roleRequest.getName())
                .description(roleRequest.getDescription())
                .permissions(permissions)
                .build();
        roleRepository.save(role);

        // Use response mapper latter
        return RoleResponse.builder()
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(permission -> PermissionResponse.builder()
                                .name(permission.getName())
                                .description(permission.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    public Set<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(role -> RoleResponse.builder()
                        .name(role.getName())
                        .description(role.getDescription())
                        .permissions(role.getPermissions().stream()
                                .map(permission -> PermissionResponse.builder()
                                        .name(permission.getName())
                                        .description(permission.getDescription())
                                        .build())
                                .collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toSet());
    }

    public void addPermissionToRole(String roleName, String permissionName) {
        Role role = roleRepository.findById(roleName).orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));

        Permission newPermission = permissionRepository
                .findById(permissionName)
                .orElseThrow(() -> new CustomException(ErrorCode.PERMISSION_NOT_FOUND));

        if (!role.getPermissions().contains(newPermission)) {
            role.getPermissions().add(newPermission);
            roleRepository.save(role);
        }
    }
}
