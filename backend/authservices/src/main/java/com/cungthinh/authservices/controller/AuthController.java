package com.cungthinh.authservices.controller;

import java.text.ParseException;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cungthinh.authservices.dto.request.LoginRequest;
import com.cungthinh.authservices.dto.request.LogoutRequest;
import com.cungthinh.authservices.dto.request.RefreshTokenRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.dto.response.LoginResponse;
import com.cungthinh.authservices.service.AuthenticationService;

import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@RequestMapping("api/v1/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse result = authService.login(loginRequest);
        ApiResponse<Object> response = ApiResponse.success(result, "Đăng nhập thành công");
        return ResponseEntity.ok(response);
    }

    // HttpServletRequest: Toàn bộ request object
    // RequestHeader: chỉ lấy thông tin header từ request

    @GetMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest.getToken());
        ApiResponse<Object> response = ApiResponse.success(null, "Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) throws ParseException {
        LoginResponse result = authService.refresh(refreshTokenRequest.getToken());
        ApiResponse<Object> response = ApiResponse.success(result, "Refresh token thành công");
        return ResponseEntity.ok(response);
    }

    // @GetMapping("/logout")
    // public ResponseEntity<?> logout(@RequestHeader("Authorization") String
    // authorizationHeader) {

    // if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer
    // ")) {
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Header bị thiếu
    // hoặc không hợp lệ");
    // }

    // String token = authorizationHeader.substring(7);
    // blackListService.addToBlackList(token);

    // return ResponseEntity.ok("Đăng xuất thành công");
    // }

    // @GetMapping("/refresh-token")
    // public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String
    // authorizationHeader) {
    // if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer
    // ")) {
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Header bị thiếu
    // hoặc không hợp lệ");
    // }

    // String token = authorizationHeader.substring(7);
    // Object result = userService.refreshToken(token);

    // if(result instanceof RefreshTokenDTO) {
    // return ResponseEntity.ok(result);
    // } else if (result instanceof ErrorResource) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    // }

    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi
    // xảy ra");
    // }
}
