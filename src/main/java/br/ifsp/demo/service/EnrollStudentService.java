package br.ifsp.demo.service;

import br.ifsp.demo.domain.ClassSchedule;
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

    private static final int MAX_CREDITS_PER_TERM = 20;

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollStudentService(EnrollmentRepository enrollmentRepository,
                                CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    public void enroll(Student student, List<String> courseCodes, Term term) {
        if (courseCodes == null || courseCodes.isEmpty()) {
            throw new BusinessRuleException("No courses provided for enrollment");
        }

        for (String courseCode : courseCodes) {
            Course course = courseRepository.findByCode(courseCode)
                    .orElseThrow(() -> new BusinessRuleException("Course not found: " + courseCode));

            validateCourseAlreadyCompleted(student, course);
            validatePrerequisites(student, course);
            validateCreditLimit(student, course, term);
            validateScheduleConflict(student, course, term);
            validateSeatsAvailability(course);

            Enrollment enrollment = new Enrollment(student, course, term);
            enrollmentRepository.save(enrollment);

            course.decreaseSeat();
            courseRepository.save(course);
        }
    }

    private void validateCourseAlreadyCompleted(Student student, Course course) {
        if (student.getCompletedCourses().contains(course.getCode())) {
            throw new BusinessRuleException("Course already completed: " + course.getCode());
        }
    }

    private void validatePrerequisites(Student student, Course course) {
        if (course.getPrerequisites() == null || course.getPrerequisites().isEmpty()) return;

        for (String prereq : course.getPrerequisites()) {
            if (!student.getCompletedCourses().contains(prereq)) {
                throw new BusinessRuleException("Missing prerequisite: " + prereq);
            }
        }
    }

    private void validateCreditLimit(Student student, Course newCourse, Term term) {
        List<Enrollment> currentEnrollments =
                enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term);

        int currentCredits = currentEnrollments.stream()
                .mapToInt(e -> e.getCourse().getCredits())
                .sum();

        int total = currentCredits + newCourse.getCredits();
        if (total > MAX_CREDITS_PER_TERM) {
            throw new BusinessRuleException("Maximum of " + MAX_CREDITS_PER_TERM + " credits exceeded");
        }
    }

    private void validateScheduleConflict(Student student, Course newCourse, Term term) {
        List<Enrollment> currentEnrollments =
                enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term);

        for (Enrollment e : currentEnrollments) {
            Course enrolled = e.getCourse();
            for (ClassSchedule existingSchedule : enrolled.getSchedule()) {
                for (ClassSchedule newSchedule : newCourse.getSchedule()) {
                    if (existingSchedule.conflictsWith(newSchedule)) {
                        throw new BusinessRuleException(
                                "Schedule conflict detected between " + enrolled.getCode() +
                                        " and " + newCourse.getCode()
                        );
                    }
                }
            }
        }
    }

    private void validateSeatsAvailability(Course course) {
        if (course.getAvailableSeats() <= 0) {
            throw new BusinessRuleException("No seats available for course: " + course.getCode());
        }
    }
}
