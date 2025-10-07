package br.ifsp.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course")
@Getter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;
    private String schedule;
    private int credits;
    private int vacancies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    public Course(String courseName, String schedule, int credits, int vacancies, Enrollment enrollment) {
        this.courseName = courseName;
        this.schedule = schedule;
        this.credits = credits;
        this.vacancies = vacancies;
        this.enrollment = enrollment;
    }
}
