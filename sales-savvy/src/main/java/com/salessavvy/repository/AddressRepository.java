
package com.salessavvy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salessavvy.entity.Address;

public interface AddressRepository
        extends JpaRepository<Address, Long> {

    List<Address> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Address> findByIdAndUser_Id(
            Long addressId, Long userId);

    List<Address> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
}
