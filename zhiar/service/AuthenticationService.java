package com.zhiar.service;


import com.zhiar.POJO.User;
import com.zhiar.dao.UserDao;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserDao userDao;

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean authenticateUser(String email, String rawPassword) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            return false; // User not found
        }
        return rawPassword.matches(rawPassword);
    }
}
