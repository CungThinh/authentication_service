package com.cungthinh.authservices.dto.request;

import jakarta.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    @NotEmpty(message = "Role không được dể trống")
    private String roleToAdd;
}
