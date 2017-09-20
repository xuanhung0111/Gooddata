package com.gooddata.qa.graphene.entity.add;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.nonNull;

public class IncrementalPeriod {

    private LocalDateTime from;
    private LocalDateTime to;

    private IncrementalPeriod() {}

    private IncrementalPeriod(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public static IncrementalPeriod create(LocalDateTime from, LocalDateTime to) {
        return new IncrementalPeriod(from, to);
    }

    public static IncrementalPeriod create(String startDateTime, String endDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime from = nonNull(startDateTime) ? LocalDateTime.parse(startDateTime, formatter) : null;
        LocalDateTime to = nonNull(endDateTime) ? LocalDateTime.parse(endDateTime, formatter) : null;
        return create(from, to);
    }

    public static IncrementalPeriod from(LocalDateTime from) {
        return create(from, null);
    }

    public static IncrementalPeriod from(String startDateTime) {
        return create(startDateTime, null);
    }

    public static IncrementalPeriod to(LocalDateTime to) {
        return create(null, to);
    }

    public static IncrementalPeriod to(String endDateTime) {
        return create(null, endDateTime);
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }
}
