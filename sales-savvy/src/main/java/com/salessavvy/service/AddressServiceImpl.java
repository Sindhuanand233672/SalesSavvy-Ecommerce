
package com.salessavvy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.AddressRequest;
import com.salessavvy.dto.AddressResponse;
import com.salessavvy.entity.Address;
import com.salessavvy.entity.User;
import com.salessavvy.repository.AddressRepository;
import com.salessavvy.repository.UserRepository;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(
            AddressRepository addressRepository,
            UserRepository userRepository) {

        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                    )
                );
    }

    private Address getOwnedAddress(
            Long addressId, Long userId) {

        return addressRepository
                .findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Address not found"
                    )
                );
    }

    private void clearDefaultAddresses(Long userId) {

        List<Address> addresses =
                addressRepository.findByUser_Id(userId);

        for (Address address : addresses) {
            address.setDefaultAddress(false);
        }

        addressRepository.saveAll(addresses);
    }

    private void copyRequestToEntity(
            AddressRequest request, Address address) {

        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setAddressType(
                request.getAddressType().toUpperCase()
        );
    }

    private AddressResponse convertToResponse(
            Address address) {

        AddressResponse response = new AddressResponse();

        response.setId(address.getId());
        response.setUserId(address.getUser().getId());
        response.setRecipientName(address.getRecipientName());
        response.setPhoneNumber(address.getPhoneNumber());
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setLandmark(address.getLandmark());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setAddressType(address.getAddressType());
        response.setDefaultAddress(address.getDefaultAddress());
        response.setCreatedAt(address.getCreatedAt());

        return response;
    }

    @Override
    public AddressResponse addAddress(
            String email, AddressRequest request) {

        User user = getUser(email);

        Address address = new Address();

        address.setUser(user);
        copyRequestToEntity(request, address);

        boolean firstAddress =
                !addressRepository.existsByUser_Id(user.getId());

        boolean makeDefault =
                firstAddress ||
                Boolean.TRUE.equals(request.getDefaultAddress());

        if (makeDefault) {
            clearDefaultAddresses(user.getId());
        }

        address.setDefaultAddress(makeDefault);

        Address savedAddress =
                addressRepository.save(address);

        return convertToResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String email) {

        User user = getUser(email);

        return addressRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(
            String email, Long addressId) {

        User user = getUser(email);

        Address address =
                getOwnedAddress(addressId, user.getId());

        return convertToResponse(address);
    }

    @Override
    public AddressResponse updateAddress(
            String email, Long addressId,
            AddressRequest request) {

        User user = getUser(email);

        Address address =
                getOwnedAddress(addressId, user.getId());

        boolean makeDefault =
                Boolean.TRUE.equals(request.getDefaultAddress());

        if (makeDefault) {
            clearDefaultAddresses(user.getId());
        }

        copyRequestToEntity(request, address);

        address.setDefaultAddress(makeDefault);

        Address updatedAddress =
                addressRepository.save(address);

        return convertToResponse(updatedAddress);
    }

    @Override
    public void deleteAddress(
            String email, Long addressId) {

        User user = getUser(email);

        Address address =
                getOwnedAddress(addressId, user.getId());

        boolean wasDefault =
                Boolean.TRUE.equals(address.getDefaultAddress());

        addressRepository.delete(address);

        if (wasDefault) {

            List<Address> remainingAddresses =
                    addressRepository
                            .findByUser_IdOrderByCreatedAtDesc(
                                    user.getId()
                            );

            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    public AddressResponse setDefaultAddress(
            String email, Long addressId) {

        User user = getUser(email);

        Address selectedAddress =
                getOwnedAddress(addressId, user.getId());

        clearDefaultAddresses(user.getId());

        selectedAddress.setDefaultAddress(true);

        Address savedAddress =
                addressRepository.save(selectedAddress);

        return convertToResponse(savedAddress);
    }
}
