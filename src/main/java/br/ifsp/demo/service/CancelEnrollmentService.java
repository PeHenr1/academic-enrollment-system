package br.ifsp.demo.service;

import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CancelEnrollmentService {

    private final EnrollmentRepository repository;

    public CancelEnrollmentService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public boolean cancelEnrollment(Long id) {

        if (id == null) { throw new NullPointerException("ID cannot be null"); }

        Enrollment enrollment = repository.findById(id).orElse(null);

        if(enrollment == null) { return false; }
        if(enrollment.isCanceled()) { throw new IllegalStateException("Enrollment Is Already Cancelled"); }
        if (enrollment.getCancellationDeadline().isBefore(LocalDate.now())) { throw new IllegalStateException("Cancellation Deadline has Expired"); }

        enrollment.cancel();
        repository.save(enrollment);
        return true;
    }
}
