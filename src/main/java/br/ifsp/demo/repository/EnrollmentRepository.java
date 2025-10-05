package br.ifsp.demo.repository;

import br.ifsp.demo.model.Enrollment;

public interface EnrollmentRepository {
    Enrollment findById(Long id);
    void save(Enrollment enrollment);
}
