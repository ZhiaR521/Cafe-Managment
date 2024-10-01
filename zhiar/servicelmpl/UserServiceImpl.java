package com.zhiar.servicelmpl;

import com.zhiar.JWT.JwtFilter;
import com.zhiar.JWT.JwtUtil;
import com.zhiar.POJO.User;
import com.zhiar.constents.CafeConstents;
import com.zhiar.dao.UserDao;
import com.zhiar.service.UserService;
import com.zhiar.utils.CafeUtils;
import com.zhiar.utils.EmailUtils;
import com.zhiar.wrapper.UserWrapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    @Autowired
    EmailUtils emailUtils;

    private final UserDao userDao;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    @Autowired
    public UserServiceImpl(UserDao userDao,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserDetailsService userDetailsService,
                           JwtFilter jwtFilter) {
        this.userDao = userDao;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    public void registerUser(User user) {
        userDao.save(user);
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("email") &&
                requestMap.containsKey("password") &&
                requestMap.containsKey("name");
    }

    public Authentication authenticate(String email, String rawPassword) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDetails, rawPassword, userDetails.getAuthorities())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Authentication failed", e);
        }
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setEmail(requestMap.get("email"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setPassword(requestMap.get("password")); // Save raw password
        user.setRole("USER");
        user.setStatus("INACTIVE");
        return user;
    }

    @Transactional
    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside Signup {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User existingUser = userDao.findByEmail(requestMap.get("email"));
                if (existingUser == null) {
                    User user = getUserFromMap(requestMap);
                    registerUser(user);
                    log.info("User successfully saved with status: {}", user.getStatus());
                    return CafeUtils.getResponseEntity("Successfully Registered.", HttpStatus.OK);
                } else {
                    log.warn("Email already exists: {}", requestMap.get("email"));
                    return CafeUtils.getResponseEntity("Email already exists.", HttpStatus.BAD_REQUEST);
                }
            } else {
                log.warn("Invalid data: {}", requestMap);
                return CafeUtils.getResponseEntity(CafeConstents.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            log.error("Exception occurred during signup", ex);
            return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try {
            Authentication auth = authenticate(requestMap.get("email"), requestMap.get("password"));
            if (auth.isAuthenticated()) {
                User user = userDao.findByEmail(requestMap.get("email"));
                if (user != null && "ACTIVE".equalsIgnoreCase(user.getStatus())) {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    return ResponseEntity.ok("{\"token\":\"" + token + "\"}");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Wait For Admin Approval.\"}");
                }
            }
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Invalid Credentials.\"}");
        } catch (Exception e) {
            log.error("Exception occurred during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\":\"Internal Server Error.\"}");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Bad Credentials.\"}");
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if (jwtFilter.isAdmin()) {
                List<UserWrapper> users = userDao.findUsersByRole("USER"); // or use "ADMIN" if needed
                return new ResponseEntity<>(users, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()) {
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
                    return CafeUtils.getResponseEntity("User Status Updated Successfully", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("User Id Does not Exist", HttpStatus.OK);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstents.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Approved", "USER:- " + user + "\n is Approved by \nADMIN:-" + jwtFilter.getCurrentUser(), allAdmin);
        } else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled", "USER:- " + user + "\n is Disabled by \nADMIN:-" + jwtFilter.getCurrentUser(), allAdmin);
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User userObj = userDao.findUByEmail(jwtFilter.getCurrentUser());
            if (userObj != null) {
                // Check if old password is correct
                if (userObj.getPassword().equals(requestMap.get("oldPassword"))) {
                    // Check if the new password is the same as the old password
                    if (userObj.getPassword().equals(requestMap.get("newPassword"))) {
                        return CafeUtils.getResponseEntity("New password cannot be the same as the old password", HttpStatus.BAD_REQUEST);
                    }
                    // Update the password
                    userObj.setPassword(requestMap.get("newPassword"));
                    userDao.save(userObj);
                    return CafeUtils.getResponseEntity("Password Updated Successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Incorrect old password", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        // Validate input
        String email = requestMap.get("email");
        if (email == null || email.trim().isEmpty()) {
            return CafeUtils.getResponseEntity("Email is required.", HttpStatus.BAD_REQUEST);
        }

        try {
            // Find the user by email
            User user = userDao.findUByEmail(email);
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                // Attempt to send the email
                try {
                    emailUtils.forgotMail(user.getEmail(), "Credentials by cafe management system", user.getPassword());
                } catch (Exception emailEx) {
                    // Handle email sending errors
                    emailEx.printStackTrace();
                    return CafeUtils.getResponseEntity("Failed to send email. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // Return success response
                return CafeUtils.getResponseEntity("Password sent to registered email.", HttpStatus.OK);
            } else {
                // Email not found
                return CafeUtils.getResponseEntity("Email not found.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

