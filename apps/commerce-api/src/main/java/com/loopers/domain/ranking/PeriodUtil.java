package com.loopers.domain.ranking;

import java.time.LocalDate;

public final class PeriodUtil {
    private PeriodUtil() {}
    public static LocalDate weekStart(LocalDate any) { return any.with(java.time.DayOfWeek.MONDAY); }
    public static LocalDate weekEnd(LocalDate any)   { return weekStart(any).plusDays(6); }
    public static LocalDate monthStart(LocalDate d)  { return d.withDayOfMonth(1); }
    public static LocalDate monthEnd(LocalDate d)    { return d.withDayOfMonth(d.lengthOfMonth()); }
}
