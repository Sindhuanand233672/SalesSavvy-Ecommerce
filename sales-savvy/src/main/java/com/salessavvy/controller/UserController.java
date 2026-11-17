
package com.salessavvy.controller;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @GetMapping("/profile")
    public Map<String, String> getProfile(Authentication authentication) {

        return Map.of(
                "message", "Profile accessed successfully",
                "email", authentication.getName(),
                "status", "Authenticated"
        );
    }
}
