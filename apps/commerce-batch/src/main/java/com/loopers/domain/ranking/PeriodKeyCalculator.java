package com.loopers.domain.ranking;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class PeriodKeyCalculator {
    private PeriodKeyCalculator() {}

    public static LocalDate weekStart(LocalDate d) {
        DayOfWeek dow = d.getDayOfWeek();
        int back = (dow.getValue() + 6) % 7;
        return d.minusDays(back);
    }
    public static LocalDate weekEnd(LocalDate d)   { return weekStart(d).plusDays(6); }
    public static LocalDate monthStart(LocalDate d){ return d.withDayOfMonth(1); }
    public static LocalDate monthEnd(LocalDate d)  { LocalDate s = monthStart(d); return s.withDayOfMonth(s.lengthOfMonth()); }
}
