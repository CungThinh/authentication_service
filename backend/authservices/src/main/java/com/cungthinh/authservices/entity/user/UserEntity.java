package com.cungthinh.authservices.entity.user;


import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="users")
@DynamicInsert
@DynamicUpdate
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String email;

    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String password;
}
