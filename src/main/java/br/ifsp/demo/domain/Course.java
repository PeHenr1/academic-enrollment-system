package br.ifsp.demo.domain;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private Long id;
    private String code;
    private String name;
    private int credits;
    private int availableSeats;
    private String shift;
    private List<ClassSchedule> schedule = new ArrayList<>();
    private List<String> prerequisites = new ArrayList<>();
    private Term term = Term.current();
    private Enrollment enrollment;

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

    public boolean hasAvailableSeats() { return availableSeats > 0; }

    public boolean hasConflictingScheduleWith(Course other) {
        for (ClassSchedule s1 : this.schedule) {
            for (ClassSchedule s2 : other.schedule) {
                if (s1.conflictsWith(s2)) return true;
            }
        }
        return false;
    }

    public boolean prerequisitesSatisfiedBy(List<String> completedCourses) {
        return prerequisites.stream().allMatch(completedCourses::contains);
    }

    public void decreaseSeat() {
        if (availableSeats > 0) availableSeats--;
        else throw new IllegalStateException("No seats available");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public List<ClassSchedule> getSchedule() { return schedule; }
    public void setSchedule(List<ClassSchedule> schedule) { this.schedule = schedule; }
    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    public Term getTerm() { return term; }
    public void setTerm(Term term) { this.term = term; }
    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }
}
