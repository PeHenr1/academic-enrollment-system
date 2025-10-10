package br.ifsp.demo.domain;

import java.time.LocalTime;

public class ClassSchedule {

    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public ClassSchedule() {}

    public ClassSchedule(String dayOfWeek, String start, String end) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = LocalTime.parse(start);
        this.endTime = LocalTime.parse(end);
    }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean conflictsWith(ClassSchedule other) {
        if (!this.dayOfWeek.equalsIgnoreCase(other.dayOfWeek)) return false;
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
}
