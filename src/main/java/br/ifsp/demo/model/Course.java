package br.ifsp.demo.model;

import lombok.Getter;

import java.util.List;

public class Course {

    private Long id;
    private String name;
    private String schedule;
    private int credits;
    private int availableSeats;
    private List<String> prerequisites;

    public Course(String code, String name, String schedule, int credits, List<String> prerequisites, int availableSeats) {
        this.code = code;
        this.name = name;
        this.schedule = schedule;
        this.credits = credits;
        this.prerequisites = prerequisites;
        this.availableSeats = availableSeats;
    }

    @Getter
    private String code;

}
