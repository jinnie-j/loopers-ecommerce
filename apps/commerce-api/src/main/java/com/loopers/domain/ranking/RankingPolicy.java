package com.loopers.domain.ranking;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RankingPolicy {

    public static final DateTimeFormatter FMT = DateTimeFormatter.BASIC_ISO_DATE;

    public static String all(LocalDate d) { return "ranking:all:" + d.format(FMT); }

    public static Instant expireAt() {
        return Instant.now().plus(Duration.ofDays(2));
    }
}
