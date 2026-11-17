package com.salessavvy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salessavvy.entity.ReturnRequest;

public interface ReturnRequestRepository
        extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByCustomer_IdOrderByCreatedAtDesc(
            Long customerId);

    List<ReturnRequest> findByOrderItem_Id(Long orderItemId);

    List<ReturnRequest> findAllByOrderByCreatedAtDesc();
}