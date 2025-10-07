package br.ifsp.demo.service;

import br.ifsp.demo.model.Course;
import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentValidationService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentValidationService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public void enrollStudent(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getAvailableSeats() <= 0) throw new IllegalStateException("No More Available Seats for This Courses");

        if (course.getCode() == null || course.getCode().isBlank()) {
            throw new IllegalArgumentException("Course With Chosen Code Not Found");
        }

        Enrollment enrollment = new Enrollment();
        enrollmentRepository.save(enrollment);
    }
}
