package com.loopers.infrastructure.event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class EventHandledKey implements Serializable {

    @Column(name = "event_id", nullable = false)
    private String eventId;
    @Column(name = "handler_name", nullable = false)
    private String handlerName;
}
