
package com.salessavvy.service;

import java.util.List;

import com.salessavvy.dto.AddressRequest;
import com.salessavvy.dto.AddressResponse;

public interface AddressService {

    AddressResponse addAddress(
            String email, AddressRequest request);

    List<AddressResponse> getMyAddresses(String email);

    AddressResponse getAddressById(
            String email, Long addressId);

    AddressResponse updateAddress(
            String email, Long addressId,
            AddressRequest request);

    void deleteAddress(String email, Long addressId);

    AddressResponse setDefaultAddress(
            String email, Long addressId);
}
