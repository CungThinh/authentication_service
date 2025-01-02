package com.cungthinh.authservices.service;

import com.cungthinh.authservices.request.LoginRequest;

public interface UserService {
    Object login(LoginRequest loginRequest);
}

