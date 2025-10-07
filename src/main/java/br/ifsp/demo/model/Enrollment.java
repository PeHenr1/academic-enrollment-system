package br.ifsp.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDate;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "enrollment")
@Getter
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate cancellationDeadline;

    private boolean canceled = false;

    public Enrollment() {
        this.cancellationDeadline = LocalDate.now().plusDays(3);
    }
    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses;

    public Enrollment(Long id) {
        this.id = id;
        this.cancellationDeadline = LocalDate.now().plusDays(3);
    }

    public void setDeadline(LocalDate date){
        this.cancellationDeadline = date;
    }

    public void cancel() {
        this.canceled = true;
    }
}
