package com.loopers.domain.ranking;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RankingPolicy {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    public static final DateTimeFormatter FMT = DateTimeFormatter.BASIC_ISO_DATE;

    public static String all(LocalDate d) { return "ranking:all:" + d.format(FMT); }

    public static Instant expireAt(LocalDate d) {
        return d.plusDays(2).atStartOfDay(ZONE).toInstant();
    }

}
