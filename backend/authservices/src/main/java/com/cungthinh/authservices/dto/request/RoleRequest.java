package com.cungthinh.authservices.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleRequest {
    String name;
    String description;

    @NotEmpty(message = "Danh sách permission không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 permission")
    Set<String> permissions;
}
