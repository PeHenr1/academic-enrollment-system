package br.ifsp.demo.domain;

import java.util.ArrayList;
import java.util.List;

public class OfferedCourse {

    private final String code;
    private final String name;
    private final int credits;
    private int availableSeats;
    private List<ClassSchedule> schedule = new ArrayList<>();
    private List<String> prerequisites = new ArrayList<>();
    private Term term = Term.current();

    public OfferedCourse(String code, String name, int credits) {
        this.code = code;
        this.name = name;
        this.credits = credits;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public List<ClassSchedule> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<ClassSchedule> schedule) {
        this.schedule = schedule;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }
}
