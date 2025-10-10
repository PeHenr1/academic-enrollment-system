package br.ifsp.demo.infrastructure.persistence;

import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
public class ClassScheduleEntity {

    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public ClassScheduleEntity() {}

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
