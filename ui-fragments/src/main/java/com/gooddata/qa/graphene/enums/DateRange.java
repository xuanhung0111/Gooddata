package com.gooddata.qa.graphene.enums;

import org.threeten.extra.YearQuarter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.TimeZone;

public enum DateRange {
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_90_DAYS("Last 90 days"),
    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    LAST_12_MONTHS("Last 12 months"),
    THIS_QUARTER("This quarter"),
    LAST_QUARTER("Last quarter"),
    LAST_4_QUARTERS("Last 4 quarters"),
    THIS_YEAR("This year"),
    LAST_YEAR("Last year");

    private String date;

    private static final ZoneId ZONE_ID = ZoneId.of("America/Los_Angeles");

    DateRange(String date) {
        this.date = date;
    }

    public LocalDate getFrom() {
        LocalDate from;
        switch (this) {
            case LAST_7_DAYS:
                from = LocalDate.now(ZONE_ID).minusDays(6);
                break;
            case LAST_30_DAYS:
                from = LocalDate.now(ZONE_ID).minusDays(29);
                break;
            case LAST_90_DAYS:
                from = LocalDate.now(ZONE_ID).minusDays(89);
                break;
            case THIS_MONTH:
                from = LocalDate.now(ZONE_ID).with(TemporalAdjusters.firstDayOfMonth());
                break;
            case LAST_MONTH:
                from = LocalDate.now(ZONE_ID).minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                break;
            case LAST_12_MONTHS:
                from = LocalDate.now(ZONE_ID).minusMonths(11).with(TemporalAdjusters.firstDayOfMonth());
                break;
            case THIS_QUARTER:
                from = YearQuarter.now(ZONE_ID).atDay(1);
                break;
            case LAST_QUARTER:
                from = YearQuarter.now(ZONE_ID).minusQuarters(1).atDay(1);
                break;
            case LAST_4_QUARTERS:
                from = YearQuarter.now(ZONE_ID).minusQuarters(3).atDay(1);
                break;
            case THIS_YEAR:
                from = LocalDate.now(ZONE_ID).with(TemporalAdjusters.firstDayOfYear());
                break;
            case LAST_YEAR:
                from = LocalDate.now(ZONE_ID).minus(1, ChronoUnit.YEARS).with(TemporalAdjusters.firstDayOfYear());
                break;
            default:
                from = LocalDate.now(ZONE_ID);
        }

        return from;
    }

    public LocalDate getTo() {
        LocalDate to;

        switch (this) {
            case THIS_MONTH:
            case LAST_12_MONTHS:
                to = LocalDate.now(ZONE_ID).with(TemporalAdjusters.lastDayOfMonth());
                break;
            case LAST_MONTH:
                to = LocalDate.now(ZONE_ID).minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                break;
            case THIS_QUARTER:
            case LAST_4_QUARTERS:
                to = YearQuarter.now(ZONE_ID).atEndOfQuarter();
                break;
            case LAST_QUARTER:
                to = YearQuarter.now(ZONE_ID).minusQuarters(1).atEndOfQuarter();
                break;
            case THIS_YEAR:
                to = LocalDate.now(ZONE_ID).with(TemporalAdjusters.lastDayOfYear());
                break;
            case LAST_YEAR:
                to = LocalDate.now(ZONE_ID).minusYears(1).with(TemporalAdjusters.lastDayOfYear());
                break;
            default:
                to = LocalDate.now(ZONE_ID);
        }

        return to;
    }

    @Override
    public String toString() {
        return date;
    }
}
