package com.cungthinh.authservices.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.cungthinh.authservices.dto.request.LoginRequest;
import com.cungthinh.authservices.dto.response.ApiResponse;
import com.cungthinh.authservices.dto.response.LoginResponse;
import com.cungthinh.authservices.dto.response.UserLoginResponse;
import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.exception.CustomException;
import com.cungthinh.authservices.exception.ErrorCode;
import com.cungthinh.authservices.repository.UserResipotory;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;

@Service
public class AuthenticationService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserResipotory userResipotory;

    public ApiResponse<Object> login(LoginRequest loginRequest) {
        var user = userResipotory.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);
        UserLoginResponse userLoginResponse = new UserLoginResponse(user.getId(), user.getEmail(), user.getRoles());
        LoginResponse loginResponse = new LoginResponse(token, userLoginResponse);
        ApiResponse<Object> apiResponse = ApiResponse.success(loginResponse, "Đăng nhập thành công");
        return apiResponse;
    }

    public String generateToken(UserEntity user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getId()).issuer("cungthinh").claim("email", user.getEmail()).claim("scope", buildScope(user))
                .issueTime(new Date()).expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Khởi tạo token bị lỗi");
        }
    }

    public String buildScope(UserEntity user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(s -> stringJoiner.add(s));
        }

        return stringJoiner.toString();
    }
}
