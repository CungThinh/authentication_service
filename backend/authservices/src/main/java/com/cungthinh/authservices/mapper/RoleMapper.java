package com.cungthinh.authservices.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cungthinh.authservices.dto.request.RoleRequest;
import com.cungthinh.authservices.dto.response.RoleResponse;
import com.cungthinh.authservices.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
