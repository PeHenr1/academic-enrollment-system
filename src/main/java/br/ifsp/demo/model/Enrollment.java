package br.ifsp.demo.model;

import lombok.Getter;

import java.time.LocalDate;

public class Enrollment {

    private Long id;

    public Enrollment(Long id) {
        this.id = id;
        this.cancellationDeadline = LocalDate.now().plusDays(3);
    }

    public void cancel() {
        this.canceled = true;
    }

    @Getter
    private LocalDate cancellationDeadline;

    @Getter
    private boolean canceled = false;
}
