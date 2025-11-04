package br.ifsp.demo.service;

import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.domain.Course;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentQueryService(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

   public List<Enrollment> getEnrollmentsByStudent(String studentId, Term term) {
        if (studentId == null || term == null) {
            throw new IllegalArgumentException("Student ID and Term cannot be null");
        }

       List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentAndTerm(studentId, term);

        if (enrollments.isEmpty()) {
            throw new NoCoursesFoundException("No enrollments found for this student this term.");
        }

        return enrollments;
    }
}