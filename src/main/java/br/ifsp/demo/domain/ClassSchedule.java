package br.ifsp.demo.domain;

import java.time.LocalTime;

public class ClassSchedule {

    private final String dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public ClassSchedule(String dayOfWeek, String start, String end) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = LocalTime.parse(start);
        this.endTime = LocalTime.parse(end);
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean conflictsWith(ClassSchedule other) {
        if (!this.dayOfWeek.equalsIgnoreCase(other.dayOfWeek)) return false;
        return (this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime));
    }
}
