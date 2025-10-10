package br.ifsp.demo.service;

import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.model.Course;
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

    public List<Course> getCoursesByEnrollment(Long enrollmentId) {
        if (enrollmentId == null) throw new IllegalArgumentException("ID cannot be null");

        if (!enrollmentRepository.existsById(enrollmentId)) throw new EnrollmentNotFoundException("Enrollment not found or inactive");

        List<Course> courses = courseRepository.findByEnrollmentId(enrollmentId);
        if (courses.isEmpty()) throw new NoCoursesFoundException("No courses found for this enrollment");

        return courses;
    }
}