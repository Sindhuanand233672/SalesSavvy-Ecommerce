package com.salessavvy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salessavvy.dto.CreateReturnRequest;
import com.salessavvy.dto.ReviewReturnRequest;
import com.salessavvy.dto.ReturnResponse;
import com.salessavvy.service.ReturnService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/returns")
@Validated
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    // CUSTOMER: Create return request
    @PostMapping("/order/{orderId}/items/{orderItemId}")
    public ResponseEntity<ReturnResponse> createReturn(
            Authentication authentication,
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @Valid @RequestBody CreateReturnRequest request) {

        ReturnResponse response = returnService.createReturn(
                authentication.getName(),
                orderId,
                orderItemId,
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // CUSTOMER: View own returns
    @GetMapping("/my-returns")
    public ResponseEntity<List<ReturnResponse>> getMyReturns(
            Authentication authentication) {

        return ResponseEntity.ok(
                returnService.getMyReturns(authentication.getName()));
    }

    // CUSTOMER / ADMIN: View one return
    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponse> getReturnById(
            Authentication authentication,
            @PathVariable Long returnId) {

        return ResponseEntity.ok(
                returnService.getReturnById(
                        authentication.getName(),
                        returnId));
    }

    // ADMIN: View all returns
    @GetMapping("/admin/all")
    public ResponseEntity<List<ReturnResponse>> getAllReturns(
            Authentication authentication) {

        return ResponseEntity.ok(
                returnService.getAllReturns(authentication.getName()));
    }

    // ADMIN: Approve return
    @PutMapping("/admin/{returnId}/approve")
    public ResponseEntity<ReturnResponse> approveReturn(
            Authentication authentication,
            @PathVariable Long returnId,
            @Valid @RequestBody ReviewReturnRequest request) {

        return ResponseEntity.ok(
                returnService.approveReturn(
                        authentication.getName(),
                        returnId,
                        request));
    }

    // ADMIN: Reject return
    @PutMapping("/admin/{returnId}/reject")
    public ResponseEntity<ReturnResponse> rejectReturn(
            Authentication authentication,
            @PathVariable Long returnId,
            @Valid @RequestBody ReviewReturnRequest request) {

        return ResponseEntity.ok(
                returnService.rejectReturn(
                        authentication.getName(),
                        returnId,
                        request));
    }

    // ADMIN: Confirm receipt and restore stock
    @PutMapping("/admin/{returnId}/received")
    public ResponseEntity<ReturnResponse> receiveReturn(
            Authentication authentication,
            @PathVariable Long returnId,
            @Valid @RequestBody ReviewReturnRequest request) {

        return ResponseEntity.ok(
                returnService.receiveReturn(
                        authentication.getName(),
                        returnId,
                        request));
    }
}