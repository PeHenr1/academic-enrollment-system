package br.ifsp.demo.infrastructure.persistence;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "enrollment")
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Embedded
    private Term term;

    private LocalDate cancellationDeadline;
    private boolean canceled = false;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses;

    public EnrollmentEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Term getTerm() { return term; }
    public void setTerm(Term term) { this.term = term; }
    public LocalDate getCancellationDeadline() { return cancellationDeadline; }
    public void setCancellationDeadline(LocalDate cancellationDeadline) { this.cancellationDeadline = cancellationDeadline; }
    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }
    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }
}
