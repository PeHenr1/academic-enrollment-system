package br.ifsp.demo.service;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollStudentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentValidationService validationService;

    public EnrollStudentService(EnrollmentRepository enrollmentRepository,
                                CourseRepository courseRepository,
                                EnrollmentValidationService validationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.validationService = validationService;
    }

    public void enroll(Student student, List<String> courseCodes, Term term) {
        if (courseCodes == null || courseCodes.isEmpty()) {
            throw new BusinessRuleException("No courses provided for enrollment");
        }

        for (String code : courseCodes) {
            Course course = courseRepository.findByCode(code)
                    .orElseThrow(() -> new BusinessRuleException("Course not found: " + code));

            validationService.validateCourseExists(course);
            validationService.validateCourseAlreadyCompleted(student, course);
            validationService.validatePrerequisites(student, course);

            List<Enrollment> currentEnrollments =
                    enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term);

            validationService.validateCreditLimit(student, course, term, currentEnrollments);
            validationService.validateScheduleConflict(course, currentEnrollments);
            validationService.validateSeatsAvailability(course);

            Enrollment enrollment = new Enrollment(student, course, term);
            enrollmentRepository.save(enrollment);

            course.decreaseSeat();
            courseRepository.save(course);
        }
    }
}
