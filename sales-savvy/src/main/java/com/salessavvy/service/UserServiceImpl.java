
package com.salessavvy.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salessavvy.entity.User;
import com.salessavvy.entity.UserRole;
import com.salessavvy.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException("User not found"));
    }

    @Override
    public User authenticateUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("Account is inactive");
        }

        return user;
    }

    @Override
    public User updateCustomerStatus(Long customerId, boolean active) {

        User user = userRepository.findById(customerId)
                .orElseThrow(() ->
                    new RuntimeException("Customer not found"));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException(
                    "This operation is only allowed for customers");
        }

        user.setActive(active);

        return userRepository.save(user);
    }
}
