package br.ifsp.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "enrollment")
@Getter
@NoArgsConstructor
public class Enrollment {

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
    private Term term; // agora usa Term

    private LocalDate cancellationDeadline;

    private boolean canceled = false;

    // Construtor atualizado
    public Enrollment(Student student, Course course, Term term) {
        this.student = student;
        this.course = course;
        this.term = term;
        this.cancellationDeadline = LocalDate.now().plusDays(3);
    }

    public void setDeadline(LocalDate date) {
        this.cancellationDeadline = date;
    }

    public void cancel() {
        this.canceled = true;
    }
}
