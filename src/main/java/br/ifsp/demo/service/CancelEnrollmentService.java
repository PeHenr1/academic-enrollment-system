package br.ifsp.demo.service;

import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;

public class CancelEnrollmentService {

    private final EnrollmentRepository repository;

    public CancelEnrollmentService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public boolean cancelEnrollment(Long id) {
        Enrollment enrollment = repository.findById(id);

        if(enrollment == null) {
            return false;
        }

        enrollment.cancel();
        repository.save(enrollment);
        return true;
    }
}
