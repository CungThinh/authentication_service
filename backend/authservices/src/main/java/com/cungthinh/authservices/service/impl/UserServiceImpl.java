package com.cungthinh.authservices.service.impl;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cungthinh.authservices.dto.LoginResponse;
import com.cungthinh.authservices.dto.UserLoginResponse;
import com.cungthinh.authservices.entity.user.UserEntity;
import com.cungthinh.authservices.repository.UserResipotory;
import com.cungthinh.authservices.request.LoginRequest;
import com.cungthinh.authservices.resource.ErrorResource;
import com.cungthinh.authservices.service.JWTService;
import com.cungthinh.authservices.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserResipotory userRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Object login(LoginRequest loginRequest) {
        try {
            UserEntity user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null) {
                throw new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác");
            }

            if (!loginRequest.getPassword().equals(user.getPassword())) {
                throw new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác");
            }

            // Generate JWT token
            String token = jwtService.generateToken(user.getId(), user.getEmail());

            return new LoginResponse(token, new UserLoginResponse(user.getId(), user.getEmail()));
        } catch (BadCredentialsException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", e.getMessage());
            return new ErrorResource("Có vấn đề trong quá trình xác thực", errors);
        }
    }
}
