package com.cungthinh.authservices.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleUpdateRequest {
    @NotBlank(message = "Tên permission không được để trống")
    String permissions;
}
