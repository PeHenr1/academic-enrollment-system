package br.ifsp.demo.service;

import br.ifsp.demo.domain.ClassSchedule;
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
            validateCourseAlreadyCompleted(student, course);
            validateCreditLimit(student, course);
            validateScheduleConflict(student, course);
            validateSeatsAvailability(course);

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

    private void validateCourseAlreadyCompleted(Student student, OfferedCourse course) {
        if (student.getCompletedCourses().contains(course.getCode())) {
            throw new BusinessRuleException("Course already completed");
        }
    }

    private void validateCreditLimit(Student student, OfferedCourse newCourse) {
        int currentCredits = enrollmentRepository.calculateTotalCredits(student.getId(), newCourse.getTerm());
        int total = currentCredits + newCourse.getCredits();
        if (total > 20) {
            throw new BusinessRuleException("Maximum of 20 credits exceeded");
        }
    }

    private void validateScheduleConflict(Student student, OfferedCourse newCourse) {
        List<OfferedCourse> alreadyEnrolled =
                enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), newCourse.getTerm());

        for (OfferedCourse enrolled : alreadyEnrolled) {
            for (ClassSchedule a : enrolled.getSchedule()) {
                for (ClassSchedule b : newCourse.getSchedule()) {
                    if (a.conflictsWith(b)) {
                        throw new BusinessRuleException("Schedule conflict detected");
                    }
                }
            }
        }
    }

    private void validateSeatsAvailability(OfferedCourse course) {
        if (course.getAvailableSeats() <= 0) {
            throw new BusinessRuleException("No seats available");
        }
    }

}
