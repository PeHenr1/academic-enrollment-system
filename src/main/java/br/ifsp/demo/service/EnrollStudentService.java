package br.ifsp.demo.service;

import br.ifsp.demo.domain.OfferedCourse;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollStudentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollStudentService(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    public void enroll(Student student, List<String> courseCodes) {
        for (String courseCode : courseCodes) {
            OfferedCourse course = courseRepository.findByCode(courseCode)
                    .orElseThrow(() -> new BusinessRuleException("Course not found"));

            validatePrerequisites(student, course);

            enrollmentRepository.saveEnrollment(student, course);
        }
    }

    private void validatePrerequisites(Student student, OfferedCourse course) {
        if (course.getPrerequisites() == null || course.getPrerequisites().isEmpty()) return;

        for (String prereq : course.getPrerequisites()) {
            if (!student.getCompletedCourses().contains(prereq)) {
                throw new BusinessRuleException("Missing prerequisite: " + prereq);
            }
        }
    }

}
