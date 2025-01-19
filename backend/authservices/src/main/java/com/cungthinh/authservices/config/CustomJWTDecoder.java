package com.cungthinh.authservices.config;

import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.cungthinh.authservices.service.JwtBlackListService;

@Component
public class CustomJWTDecoder implements JwtDecoder {

    @Value("${jwt.secret}")
    private String secretKey;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Autowired
    private JwtBlackListService jwtBlackListService;

    @Override
    public Jwt decode(String token) throws JwtException {

        if (jwtBlackListService.isTokenBlacklisted(token)) {
            throw new JwtException("Token is blacklisted");
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
