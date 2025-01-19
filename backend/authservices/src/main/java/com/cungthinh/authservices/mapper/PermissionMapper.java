package com.cungthinh.authservices.mapper;

import org.mapstruct.Mapper;

import com.cungthinh.authservices.dto.request.PermissionRequest;
import com.cungthinh.authservices.dto.response.PermissionResponse;
import com.cungthinh.authservices.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
