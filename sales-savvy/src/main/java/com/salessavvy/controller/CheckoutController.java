
package com.salessavvy.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.dto.CheckoutRequest;
import com.salessavvy.dto.CheckoutResponse;
import com.salessavvy.service.CheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // CUSTOMER: Complete checkout and initiate payment
    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(
            Principal principal,
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutResponse response = checkoutService.checkout(
                principal.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
