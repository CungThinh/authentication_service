package com.cungthinh.authservices.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@Entity
@Table(name = "users")
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(columnDefinition = "varchar(255)", nullable = false)
    String email;

    @Column(columnDefinition = "varchar(255)", nullable = false)
    String password;

    // DOB
    LocalDate dob;

    // @Column(columnDefinition = "varchar(255)")
    // String firstName;
    //
    //    @Column(columnDefinition = "varchar(255)")
    //    String lastName;
    //
    //    @Column(columnDefinition = "varchar(255)")
    //    boolean enabled;
    //
    //    Date lastPasswordResetDate;
    //
    //    boolean isOtpRequired;

    @ManyToMany
    Set<Role> roles;
}
