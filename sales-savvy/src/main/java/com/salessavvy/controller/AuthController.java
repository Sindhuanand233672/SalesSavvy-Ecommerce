
package com.salessavvy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salessavvy.dto.AuthResponse;
import com.salessavvy.dto.LoginRequest;
import com.salessavvy.dto.RegisterRequest;
import com.salessavvy.entity.User;
import com.salessavvy.entity.UserRole;
import com.salessavvy.service.JwtService;
import com.salessavvy.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // Register a new customer
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        User savedUser = userService.registerUser(user);

        String token = jwtService.generateToken(savedUser.getEmail());

        AuthResponse response = new AuthResponse(
                "Registration successful",
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                token
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Login an existing customer
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.authenticateUser(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user.getEmail());

        AuthResponse response = new AuthResponse(
                "Login successful",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                token
        );

        return ResponseEntity.ok(response);
    }
}
