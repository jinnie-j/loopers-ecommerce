package com.loopers.infrastructure.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventHandledKey implements Serializable {
    private String eventId;
    private String handlerName;
}
