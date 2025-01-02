package com.cungthinh.authservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cungthinh.authservices.repository.UserResipotory;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    @Autowired
    private UserResipotory userResipotory;

    @GetMapping("/me")
    public String me() {
        return "Hello World";
    }
}
