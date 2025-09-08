package com.loopers.domain.audit;

public record AuditCommand(String eventId, String topic, String key, String payloadJson) {}
