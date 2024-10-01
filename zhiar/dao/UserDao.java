package com.zhiar.dao;

import com.zhiar.POJO.User;
import com.zhiar.wrapper.UserWrapper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserDao extends JpaRepository<User, Integer> {

    User findByEmail(@Param("email") String email);

    List<UserWrapper> findUsersByRole(@Param("role") String role);

    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);

    List<String> getAllAdmin();

    User findUByEmail(String email);

}
