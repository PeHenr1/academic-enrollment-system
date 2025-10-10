package br.ifsp.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
    private int credits;
    private int availableSeats;
    private String shift;

    @ElementCollection
    @CollectionTable(name = "course_schedules", joinColumns = @JoinColumn(name = "course_id"))
    private List<ClassSchedule> schedule = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "course_prerequisites", joinColumns = @JoinColumn(name = "course_id"))
    private List<String> prerequisites = new ArrayList<>();

    @Embedded
    private Term term = Term.current();

    public Course() {}

    public Course(String code, String name, int credits) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.availableSeats = 0;
    }

    public Course(String code, String name, int credits, List<String> prerequisites, int availableSeats) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
        this.availableSeats = availableSeats;
    }

    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }

    public boolean hasConflictingScheduleWith(Course other) {
        for (ClassSchedule s1 : this.schedule) {
            for (ClassSchedule s2 : other.schedule) {
                if (s1.conflictsWith(s2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean prerequisitesSatisfiedBy(List<String> completedCourses) {
        return prerequisites.stream().allMatch(completedCourses::contains);
    }

    public void decreaseSeat() {
        if (availableSeats > 0) {
            availableSeats--;
        } else {
            throw new IllegalStateException("No seats available");
        }
    }
}
