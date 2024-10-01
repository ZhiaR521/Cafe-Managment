package com.zhiar.POJO;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@NamedQuery(name = "User.getAllUser", query = "SELECT new com.zhiar.wrapper.UserWrapper(u.id, u.name, u.email, u.contactNumber, u.status) FROM User u WHERE u.role = 'user'")

@NamedQuery(name = "User.updateStatus", query = "update User u set u.status=:status where u.id=:id")

@NamedQuery(name = "User.getAllAdmin", query = "SELECT u.email  FROM User u WHERE u.role = 'admin'")


@Entity
@Table(name = "users")
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "contactNumber")
    private String contactNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "role")
    private String role;

    // Constructor for email and password only
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}