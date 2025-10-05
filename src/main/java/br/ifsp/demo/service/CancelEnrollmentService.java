package br.ifsp.demo.service;

import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;

import java.time.LocalDate;

public class CancelEnrollmentService {

    private final EnrollmentRepository repository;

    public CancelEnrollmentService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public boolean cancelEnrollment(Long id) {

        if (id == null) { throw new NullPointerException("ID cannot be null"); }

        Enrollment enrollment = repository.findById(id);

        if(enrollment == null) { return false; }
        if (enrollment.getCancellationDeadline().isBefore(LocalDate.now())) { throw new IllegalStateException("Cancellation Deadline has Expired"); }

        enrollment.cancel();
        repository.save(enrollment);
        return true;
    }
}
