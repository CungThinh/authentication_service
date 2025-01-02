package com.cungthinh.authservices.service.impl;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cungthinh.authservices.repository.UserResipotory;

import com.cungthinh.authservices.entity.user.UserEntity;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserResipotory userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) {
        UserEntity user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                new ArrayList<>());
    }

}
