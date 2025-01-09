package com.cungthinh.authservices.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String jwtToken;
    // private String refreshToken;
    private UserLoginResponse user;
}
