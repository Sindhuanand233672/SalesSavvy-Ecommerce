package com.salessavvy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.CreateReturnRequest;
import com.salessavvy.dto.ReviewReturnRequest;
import com.salessavvy.dto.ReturnResponse;
import com.salessavvy.entity.InventoryMovement;
import com.salessavvy.entity.InventoryMovementType;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderItem;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.Product;
import com.salessavvy.entity.ReturnRequest;
import com.salessavvy.entity.ReturnStatus;
import com.salessavvy.entity.User;
import com.salessavvy.entity.UserRole;
import com.salessavvy.repository.InventoryMovementRepository;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.ReturnRequestRepository;
import com.salessavvy.repository.UserRepository;

@Service
@Transactional
public class ReturnServiceImpl implements ReturnService {

    private final ReturnRequestRepository returnRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    public ReturnServiceImpl(
            ReturnRequestRepository returnRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            InventoryMovementRepository movementRepository) {

        this.returnRepository = returnRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private User getAdmin(String email) {
        User user = getUser(email);

        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can perform this action");
        }

        return user;
    }

    private ReturnRequest getReturn(Long returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Return request not found"));
    }

    private ReturnResponse convertToResponse(
            ReturnRequest returnRequest) {

        OrderItem item = returnRequest.getOrderItem();

        return new ReturnResponse(
                returnRequest.getId(),
                returnRequest.getOrder().getId(),
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                returnRequest.getCustomer().getEmail(),
                returnRequest.getQuantity(),
                returnRequest.getReason(),
                returnRequest.getStatus(),
                returnRequest.getAdminNote(),
                returnRequest.getCreatedAt(),
                returnRequest.getReviewedAt(),
                returnRequest.getCompletedAt());
    }

    private void verifyOwnership(
            ReturnRequest returnRequest, User user) {

        if (!returnRequest.getCustomer().getId()
                .equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another customer's return request");
        }
    }

    @Override
    public ReturnResponse createReturn(
            String email,
            Long orderId,
            Long orderItemId,
            CreateReturnRequest request) {

        User customer = getUser(email);

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only customers can request product returns");
        }

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(customer.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot return items from another customer's order");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Returns are allowed only for delivered orders");
        }

        OrderItem orderItem = order.getOrderItems()
                .stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order item does not belong to this order"));

        if (request.getQuantity() > orderItem.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Return quantity cannot exceed purchased quantity");
        }

        List<ReturnRequest> previousRequests =
                returnRepository.findByOrderItem_Id(orderItemId);

        int alreadyRequested = previousRequests.stream()
                .filter(r -> r.getStatus() != ReturnStatus.REJECTED)
                .mapToInt(ReturnRequest::getQuantity)
                .sum();

        if (request.getQuantity()
                > orderItem.getQuantity() - alreadyRequested) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Requested quantity exceeds the remaining returnable quantity");
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrder(order);
        returnRequest.setOrderItem(orderItem);
        returnRequest.setCustomer(customer);
        returnRequest.setQuantity(request.getQuantity());
        returnRequest.setReason(request.getReason().trim());
        returnRequest.setStatus(ReturnStatus.REQUESTED);

        return convertToResponse(returnRepository.save(returnRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnResponse> getMyReturns(String email) {

        User customer = getUser(email);

        return returnRepository
                .findByCustomer_IdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnResponse getReturnById(
            String email, Long returnId) {

        User user = getUser(email);
        ReturnRequest returnRequest = getReturn(returnId);

        if (user.getRole() != UserRole.ADMIN) {
            verifyOwnership(returnRequest, user);
        }

        return convertToResponse(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnResponse> getAllReturns(String email) {

        getAdmin(email);

        return returnRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReturnResponse approveReturn(
            String email,
            Long returnId,
            ReviewReturnRequest request) {

        getAdmin(email);

        ReturnRequest returnRequest = getReturn(returnId);

        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only requested returns can be approved");
        }

        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setAdminNote(request.getAdminNote().trim());
        returnRequest.setReviewedAt(LocalDateTime.now());

        return convertToResponse(returnRepository.save(returnRequest));
    }

    @Override
    public ReturnResponse rejectReturn(
            String email,
            Long returnId,
            ReviewReturnRequest request) {

        getAdmin(email);

        ReturnRequest returnRequest = getReturn(returnId);

        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only requested returns can be rejected");
        }

        returnRequest.setStatus(ReturnStatus.REJECTED);
        returnRequest.setAdminNote(request.getAdminNote().trim());
        returnRequest.setReviewedAt(LocalDateTime.now());

        return convertToResponse(returnRepository.save(returnRequest));
    }

    @Override
    public ReturnResponse receiveReturn(
            String email,
            Long returnId,
            ReviewReturnRequest request) {

        getAdmin(email);

        ReturnRequest returnRequest = getReturn(returnId);

        if (returnRequest.getStatus() != ReturnStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only approved returns can be marked as received");
        }

        OrderItem orderItem = returnRequest.getOrderItem();

        Product product = productRepository.findByIdForUpdate(
                orderItem.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        int previousStock = product.getStockQuantity();
        int newStock;

        try {
            newStock = Math.addExact(
                    previousStock,
                    returnRequest.getQuantity());
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock quantity overflow");
        }

        product.setStockQuantity(newStock);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setMovementType(
                InventoryMovementType.STOCK_ADDED);
        movement.setQuantity(returnRequest.getQuantity());
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(
                "Stock restored after receiving return #"
                        + returnRequest.getId()
                        + " for order #"
                        + returnRequest.getOrder().getId());
        movement.setPerformedBy(email);

        movementRepository.save(movement);

        returnRequest.setStatus(ReturnStatus.COMPLETED);
        returnRequest.setAdminNote(request.getAdminNote().trim());
        returnRequest.setCompletedAt(LocalDateTime.now());

        return convertToResponse(returnRepository.save(returnRequest));
    }
}