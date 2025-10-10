package br.ifsp.demo.service;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.infrastructure.persistence.CourseEntity;
import br.ifsp.demo.mapper.CourseMapper;
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
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new EnrollmentNotFoundException("Enrollment Not Found or Inactive");
        }

        List<CourseEntity> entities = courseRepository.findByEnrollmentId(enrollmentId);

        if (entities.isEmpty()) {
            throw new NoCoursesFoundException("No Courses Found For This Enrollment.");
        }

        return CourseMapper.toDomainList(entities);
    }
}
