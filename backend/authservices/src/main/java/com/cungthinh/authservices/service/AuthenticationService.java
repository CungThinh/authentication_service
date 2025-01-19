package com.cungthinh.authservices.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.cungthinh.authservices.dto.request.LoginRequest;
import com.cungthinh.authservices.dto.response.*;
import com.cungthinh.authservices.entity.Permission;
import com.cungthinh.authservices.entity.UserEntity;
import com.cungthinh.authservices.exception.CustomException;
import com.cungthinh.authservices.exception.ErrorCode;
import com.cungthinh.authservices.repository.UserResipotory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refreshable-duration}")
    private long refreshable_duration;

    @Value("${jwt.expired-duration}")
    private long expired_duration;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserResipotory userResipotory;

    @Autowired
    private JwtBlackListService jwtBlackListService;

    public LoginResponse login(LoginRequest loginRequest) {
        var user = userResipotory
                .findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);
        var roles = user.getRoles();

        Set<RoleResponse> roleResponses = roles.stream()
                .map(role -> {
                    Set<Permission> permissions = role.getPermissions();
                    Set<PermissionResponse> permissionResponses = permissions.stream()
                            .map(permission -> {
                                return new PermissionResponse(permission.getName(), permission.getDescription());
                            })
                            .collect(Collectors.toSet());
                    return new RoleResponse(role.getName(), role.getDescription(), permissionResponses);
                })
                .collect(Collectors.toSet());

        UserLoginResponse userLoginResponse = new UserLoginResponse(user.getId(), user.getEmail(), roleResponses);
        return new LoginResponse(token, userLoginResponse);
    }

    public void logout(String token) {
        try {
            SignedJWT signedJWT = verifyToken(token, true);
            Long tokenExpiration =
                    signedJWT.getJWTClaimsSet().getExpirationTime().getTime();
            jwtBlackListService.addTokenToBlackList(token, tokenExpiration);
        } catch (ParseException | JOSEException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public LoginResponse refresh(String token) throws ParseException {
        try {
            SignedJWT signedJWT = verifyToken(token, true);
            String userId = signedJWT.getJWTClaimsSet().getSubject();
            var user = userResipotory.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            String newToken = generateToken(user);
            return LoginResponse.builder().jwtToken(newToken).build();
        } catch (JOSEException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String generateToken(UserEntity user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("cungthinh")
                .claim("email", user.getEmail())
                .claim("scope", buildScope(user))
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expired_duration, ChronoUnit.SECONDS).toEpochMilli()))
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
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }

        return stringJoiner.toString();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(secretKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime;
        if (isRefresh) {
            expirationTime = new Date(signedJWT
                    .getJWTClaimsSet()
                    .getIssueTime()
                    .toInstant()
                    .plus(refreshable_duration, ChronoUnit.SECONDS)
                    .toEpochMilli());
        } else {
            expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        }

        if (!signedJWT.verify(verifier)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (expirationTime.before(new Date())) {
            //            log.info("Token hết hạn");
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        }

        if (jwtBlackListService.isTokenBlacklisted(token)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
    }
}
