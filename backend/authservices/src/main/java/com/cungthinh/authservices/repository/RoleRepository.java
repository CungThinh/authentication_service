package com.cungthinh.authservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cungthinh.authservices.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {}
