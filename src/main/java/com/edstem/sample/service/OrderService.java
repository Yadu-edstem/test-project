package com.edstem.sample.service;

import com.edstem.sample.dto.request.PlaceOrderRequest;
import com.edstem.sample.dto.response.CreationResult;
import com.edstem.sample.dto.response.OrderResponse;
import com.edstem.sample.entity.CustomerOrder;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.mapper.OrderMapper;
import com.edstem.sample.repository.CustomerOrderRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final CustomerOrderRepository orderRepository;
  private final OrderProcessor orderProcessor;
  private final OrderMapper orderMapper;

  public CreationResult<OrderResponse> place(String idempotencyKey, PlaceOrderRequest request) {
    return findByKey(idempotencyKey)
        .map(CreationResult::existing)
        .orElseGet(() -> createOrReplay(idempotencyKey, request));
  }

  public OrderResponse get(UUID id) {
    return orderRepository
        .findWithItemsById(id)
        .map(orderMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException(CustomerOrder.class, id));
  }

  public OrderResponse cancel(UUID id) {
    return orderProcessor.cancel(id);
  }

  private CreationResult<OrderResponse> createOrReplay(
      String idempotencyKey, PlaceOrderRequest request) {
    try {
      return CreationResult.created(orderProcessor.create(idempotencyKey, request));
    } catch (DataIntegrityViolationException concurrentDuplicate) {
      return findByKey(idempotencyKey)
          .map(CreationResult::existing)
          .orElseThrow(() -> concurrentDuplicate);
    }
  }

  private Optional<OrderResponse> findByKey(String idempotencyKey) {
    return orderRepository.findByIdempotencyKey(idempotencyKey).map(orderMapper::toResponse);
  }
}
