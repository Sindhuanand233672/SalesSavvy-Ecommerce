
package com.salessavvy.controller;

import java.security.Principal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.dto.AddressRequest;
import com.salessavvy.dto.AddressResponse;
import com.salessavvy.service.AddressService;

@RestController
@RequestMapping("/api/addresses")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // Add a new address
    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            Principal principal,
            @Valid @RequestBody AddressRequest request) {

        AddressResponse response =
                addressService.addAddress(
                        principal.getName(), request);

        return new ResponseEntity<>(
                response, HttpStatus.CREATED);
    }

    // Get all addresses of logged-in user
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(
            Principal principal) {

        return ResponseEntity.ok(
                addressService.getMyAddresses(
                        principal.getName()));
    }

    // Get address by ID
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            Principal principal,
            @PathVariable Long addressId) {

        return ResponseEntity.ok(
                addressService.getAddressById(
                        principal.getName(), addressId));
    }

    // Update address
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            Principal principal,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(
                addressService.updateAddress(
                        principal.getName(),
                        addressId,
                        request));
    }

    // Delete address
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            Principal principal,
            @PathVariable Long addressId) {

        addressService.deleteAddress(
                principal.getName(), addressId);

        return ResponseEntity.ok(
                "Address deleted successfully");
    }

    // Set default address
    @PutMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            Principal principal,
            @PathVariable Long addressId) {

        return ResponseEntity.ok(
                addressService.setDefaultAddress(
                        principal.getName(), addressId));
    }
}
