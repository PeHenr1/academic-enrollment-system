package br.ifsp.demo.service;

import br.ifsp.demo.domain.ClassSchedule;
import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentValidationService {

    private static final int MAX_CREDITS_PER_TERM = 20;

    public void validateCourseExists(Course course) {
        if (course == null) throw new BusinessRuleException("Course not found");
        if (course.getCode() == null || course.getCode().isBlank()) {
            throw new BusinessRuleException("Course code is missing or blank");
        }
    }

    public void validateSeatsAvailability(Course course) {
        if (course.getAvailableSeats() <= 0) {
            throw new BusinessRuleException("No seats available for course: " + course.getCode());
        }
    }

    public void validateCourseAlreadyCompleted(Student student, Course course) {
        if (student.getCompletedCourses().contains(course.getCode())) {
            throw new BusinessRuleException("Course already completed: " + course.getCode());
        }
    }

    public void validatePrerequisites(Student student, Course course) {
        List<String> prerequisites = course.getPrerequisites();
        if (prerequisites == null || prerequisites.isEmpty()) return;

        for (String prereq : prerequisites) {
            if (!student.getCompletedCourses().contains(prereq)) {
                throw new BusinessRuleException("Missing prerequisite: " + prereq);
            }
        }
    }

    public void validateCreditLimit(Student student, Course newCourse, Term term, List<Enrollment> currentEnrollments) {
        int currentCredits = currentEnrollments.stream()
                .mapToInt(e -> e.getCourse().getCredits())
                .sum();

        int total = currentCredits + newCourse.getCredits();
        if (total > MAX_CREDITS_PER_TERM) {
            throw new BusinessRuleException("Maximum of " + MAX_CREDITS_PER_TERM + " credits exceeded");
        }
    }

    public void validateScheduleConflict(Course newCourse, List<Enrollment> currentEnrollments) {
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
}
