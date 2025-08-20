package com.loopers.application.payment;

public record PaymentApprovedEvent(Long orderId, Long userId, Long payableAmount, Long couponId) {}
