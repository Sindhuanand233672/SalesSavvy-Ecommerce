
package com.salessavvy.service;

import com.salessavvy.entity.User;

public interface UserService {

    User registerUser(User user);

    User getUserByEmail(String email);

    User authenticateUser(String email, String password);

    User updateCustomerStatus(Long customerId, boolean active);
}
