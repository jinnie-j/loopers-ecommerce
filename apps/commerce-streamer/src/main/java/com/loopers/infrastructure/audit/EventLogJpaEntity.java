package com.loopers.infrastructure.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="event_log")
@Getter
@NoArgsConstructor
public class EventLogJpaEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) private Long id;
    private String eventId;
    private String topic;
    private String keyStr;
    @Lob private String payloadJson;
    public static EventLogJpaEntity of(String e,String t,String k,String j){ var x=new EventLogJpaEntity(); x.eventId=e;x.topic=t;x.keyStr=k;x.payloadJson=j; return x; }
}
