package com.salessavvy.service;

import java.util.List;

import com.salessavvy.dto.CreateReturnRequest;
import com.salessavvy.dto.ReviewReturnRequest;
import com.salessavvy.dto.ReturnResponse;

public interface ReturnService {

    ReturnResponse createReturn(
            String email,
            Long orderId,
            Long orderItemId,
            CreateReturnRequest request);

    List<ReturnResponse> getMyReturns(String email);

    ReturnResponse getReturnById(String email, Long returnId);

    List<ReturnResponse> getAllReturns(String email);

    ReturnResponse approveReturn(
            String email,
            Long returnId,
            ReviewReturnRequest request);

    ReturnResponse rejectReturn(
            String email,
            Long returnId,
            ReviewReturnRequest request);

    ReturnResponse receiveReturn(
            String email,
            Long returnId,
            ReviewReturnRequest request);
}