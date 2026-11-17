
package com.salessavvy.service;

import com.salessavvy.dto.CheckoutRequest;
import com.salessavvy.dto.CheckoutResponse;

public interface CheckoutService {

    CheckoutResponse checkout(
            String email,
            CheckoutRequest request);
}
