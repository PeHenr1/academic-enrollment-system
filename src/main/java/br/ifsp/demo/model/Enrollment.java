package br.ifsp.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDate;

@Entity
@Table(name = "enrollment")
@Getter
public class Enrollment {

    @Id
    private Long id;

    private LocalDate cancellationDeadline;

    private boolean canceled = false;

    public Enrollment() {
        this.cancellationDeadline = LocalDate.now().plusDays(3);
    }

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
