package br.ifsp.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private String schedule;
    private int credits;
    private int availableSeats;
    private String shift;
    private List<String> prerequisites;

    public Course() {}

    public Course(String code, String name, String schedule, int credits, List<String> prerequisites, int availableSeats) {
        this.code = code;
        this.name = name;
        this.schedule = schedule;
        this.credits = credits;
        this.prerequisites = prerequisites;
        this.availableSeats = availableSeats;
    }
}
