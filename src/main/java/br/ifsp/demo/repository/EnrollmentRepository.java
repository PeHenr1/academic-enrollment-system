package br.ifsp.demo.repository;

import br.ifsp.demo.model.Enrollment;

import java.util.List;

public interface EnrollmentRepository {
    List<Enrollment> findByStudentId(Long studentId);
}