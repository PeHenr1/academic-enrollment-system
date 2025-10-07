package br.ifsp.demo.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "enrollment")
@Getter
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseName;
    private String schedule;
    private int credits;
    private int vacancies;

    public Enrollment(String courseName, String schedule, Integer credits, Integer vacancies) {
        this.courseName = courseName;
        this.schedule = schedule;
        this.credits = credits;
        this.vacancies = vacancies;
    }

    public Enrollment() {}

    public void setId(long l) {
    }
}

