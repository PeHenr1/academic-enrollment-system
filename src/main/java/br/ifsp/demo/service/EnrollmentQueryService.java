package br.ifsp.demo.service;

import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EnrollmentQueryService {

    private final EnrollmentRepository repository;

    public EnrollmentQueryService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public Optional<Enrollment> getEnrollmentsByStudent(Long id) {
        boolean studentExists = repository.existsById(id);
        if (!studentExists) {
            throw new EnrollmentNotFoundException("Matrícula não encontrada ou inativa");
        }

        Optional<Enrollment> enrollments = repository.findById(id);
        if (enrollments.isEmpty()) {
            throw new NoCoursesFoundException("Nenhuma disciplina encontrada para esta matrícula.");
        }

        return enrollments;
    }
}