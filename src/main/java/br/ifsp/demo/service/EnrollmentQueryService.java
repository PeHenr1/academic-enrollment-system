package br.ifsp.demo.service;

import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;

import java.util.List;

public class EnrollmentQueryService {

    private final EnrollmentRepository repository;

    public EnrollmentQueryService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        List<Enrollment> enrollments = repository.findByStudentId(studentId);

        if (enrollments.isEmpty()) {
            throw new EnrollmentNotFoundException("Matrícula não encontrada ou inativa");
        }

        return enrollments;
    }
}
