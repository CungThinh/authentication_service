package com.cungthinh.authservices.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cungthinh.authservices.dto.request.UserCreationRequest;
import com.cungthinh.authservices.dto.request.UserUpdateRequest;
import com.cungthinh.authservices.dto.response.UserResponse;
import com.cungthinh.authservices.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toUser(UserCreationRequest request);

    UserResponse toUserResponse(UserEntity user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget UserEntity user, UserUpdateRequest request);
}
