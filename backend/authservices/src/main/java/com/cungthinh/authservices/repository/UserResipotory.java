package com.cungthinh.authservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cungthinh.authservices.entity.user.UserEntity;

public interface UserResipotory extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    UserEntity findByEmail(String email);
}
