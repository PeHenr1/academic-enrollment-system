package br.ifsp.demo.service;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CancelEnrollmentService {

    private final EnrollmentRepository repository;
    private final CourseRepository courseRepository;

    public CancelEnrollmentService(EnrollmentRepository repository, CourseRepository courseRepository) {
        this.repository = repository;
        this.courseRepository = courseRepository;
    }

    public boolean cancelEnrollment(Long id, String studentId) {

        if (id == null) { throw new NullPointerException("ID cannot be null"); }

        Enrollment enrollment = repository.findById(id).orElse(null);

        if(enrollment == null) { return false; }

        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("You do not have permission to cancel this enrollment.");
        }

        if(enrollment.isCanceled()) { throw new IllegalStateException("Enrollment Is Already Cancelled"); }
        if (enrollment.getCancellationDeadline().isBefore(LocalDate.now())) { throw new IllegalStateException("Cancellation Deadline has Expired"); }

        Course course = enrollment.getCourse();
        if (course != null) {
            course.increaseSeat();
            courseRepository.save(course);
        }

        enrollment.cancel();
        repository.save(enrollment);
        return true;
    }
}