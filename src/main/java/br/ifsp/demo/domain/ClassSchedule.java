package br.ifsp.demo.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
public class ClassSchedule {

    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    protected ClassSchedule() {} // JPA requirement

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
