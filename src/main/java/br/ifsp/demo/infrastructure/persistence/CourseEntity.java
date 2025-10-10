package br.ifsp.demo.infrastructure.persistence;

import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Term;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class CourseEntity {

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
    private List<ClassScheduleEntity> schedule = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "course_prerequisites", joinColumns = @JoinColumn(name = "course_id"))
    private List<String> prerequisites = new ArrayList<>();

    @Embedded
    private Term term = Term.current();

    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private EnrollmentEntity enrollment;

    public CourseEntity() {}

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
    public List<ClassScheduleEntity> getSchedule() { return schedule; }
    public void setSchedule(List<ClassScheduleEntity> schedule) { this.schedule = schedule; }
    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    public Term getTerm() { return term; }
    public void setTerm(Term term) { this.term = term; }
    public EnrollmentEntity getEnrollment() { return enrollment; }
    public void setEnrollment(EnrollmentEntity enrollment) { this.enrollment = enrollment; }
}
