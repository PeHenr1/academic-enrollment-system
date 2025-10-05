package br.ifsp.demo.service;

import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;

import java.util.List;

public class EnrollmentQueryService {

    private final EnrollmentRepository repository;

    public EnrollmentQueryService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return repository.findByStudentId(studentId);
    }
}
