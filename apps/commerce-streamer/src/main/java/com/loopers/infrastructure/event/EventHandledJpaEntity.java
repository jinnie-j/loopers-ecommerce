package com.loopers.infrastructure.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="event_handled")
@IdClass(EventHandledKey.class)
@Getter
@NoArgsConstructor
public class EventHandledJpaEntity {
    @Id
    @Column(name="event_id")     private String eventId;
    @Id @Column(name="handler_name") private String handlerName;
    @Column(name="processed_at", nullable=false) private LocalDateTime processedAt;

    public static EventHandledJpaEntity of(String eventId, String handler){
        var e = new EventHandledJpaEntity();
        e.eventId = eventId; e.handlerName = handler; e.processedAt = LocalDateTime.now();
        return e;
    }
}
