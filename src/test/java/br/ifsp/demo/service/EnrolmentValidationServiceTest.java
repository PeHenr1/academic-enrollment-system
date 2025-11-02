package br.ifsp.demo.service;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import br.ifsp.demo.domain.ClassSchedule;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import br.ifsp.demo.repository.StudentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Tag("UnitTest")
@Tag("TDD")
class EnrollmentValidationServiceTest {

    private EnrollmentValidationService service;

    private Student student;
    private Course course;
    private Term term;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new EnrollmentValidationService();

        student = TestUtils.createStudentWithCompletedCourses("123", "John Doe", List.of());
        term = TestUtils.createDefaultTerm();
        course = TestUtils.createCourse("IFSP101", "Software Engineering", 4);
        course.setAvailableSeats(5);
        course.setSchedule(List.of());
        course.setPrerequisites(List.of());
    }

    @Test
    void shouldRejectWhenCourseIsNull() {
        assertThatThrownBy(() -> service.validateCourseExists(null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course not found");
    }

    @Test
    void shouldRejectWhenCourseCodeIsNull() {
        course.setCode(null);
        assertThatThrownBy(() -> service.validateCourseExists(course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course code is missing or blank");
    }

    @Test
    void shouldRejectWhenCourseCodeIsBlank() {
        course.setCode("   ");
        assertThatThrownBy(() -> service.validateCourseExists(course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course code is missing or blank");
    }

    @Test
    void shouldRejectWhenNoSeatsAvailable() {
        course.setAvailableSeats(0);
        assertThatThrownBy(() -> service.validateSeatsAvailability(course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }

    @Test
    void shouldRejectWhenCourseAlreadyCompleted() {
        student = TestUtils.createStudentWithCompletedCourses("123", "John Doe", List.of("IFSP101"));
        assertThatThrownBy(() -> service.validateCourseAlreadyCompleted(student, course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Course already completed");
    }

    @Test
    void shouldRejectWhenPrerequisiteNotCompleted() {
        course = TestUtils.createCourseWithPrerequisites("IFSP101", "Software Engineering", 4, List.of("IFSP201"));
        assertThatThrownBy(() -> service.validatePrerequisites(student, course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    void shouldRejectWhenCreditLimitExceeded() {
        course.setCredits(15);
        Enrollment existingEnrollment = TestUtils.createEnrollment(student, TestUtils.createCourse("IFSP102", "Algorithms", 10), term);
        List<Enrollment> currentEnrollments = List.of(existingEnrollment);

        assertThatThrownBy(() -> service.validateCreditLimit(student, course, term, currentEnrollments))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximum of 20 credits exceeded");
    }

    @Test
    void shouldRejectWhenScheduleConflictDetected() {
        course = TestUtils.createCourseWithSchedule("IFSP101", "Software Engineering", 4,
                List.of(TestUtils.createClassSchedule("Monday", "10:00", "12:00")));

        Course enrolledCourse = TestUtils.createCourseWithSchedule("IFSP102", "Algorithms", 4,
                List.of(TestUtils.createClassSchedule("Monday", "11:00", "13:00")));

        Enrollment existingEnrollment = TestUtils.createEnrollment(student, enrolledCourse, term);

        assertThatThrownBy(() -> service.validateScheduleConflict(course, List.of(existingEnrollment)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected");
    }
}

@Tag("Functional")
@Tag("UnitTest")
@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnrollmentValidationServiceFunctionalTest {

    @Autowired
    private EnrollmentValidationService service;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Student student;
    private Term term;
    private Course course;

    @BeforeEach
    void setupDatabase() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();

        student = TestUtils.createStudentWithCompletedCourses("123", "John Doe", List.of());
        studentRepository.save(student);

        term = Term.current();

        course = TestUtils.createCourse("IFSP101", "Software Engineering", 4);
        course.setAvailableSeats(5);
        course.setSchedule(List.of());
        course.setPrerequisites(List.of());
        courseRepository.save(course);
    }

    @Test
    void shouldRejectWhenCourseIsNull() {
        assertThatThrownBy(() -> service.validateCourseExists(null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course not found");
    }

    @Test
    void shouldRejectWhenCourseCodeIsNull() {
        course.setCode(null);
        courseRepository.save(course);

        assertThatThrownBy(() -> service.validateCourseExists(course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course code is missing or blank");
    }

    @Test
    void shouldRejectWhenNoSeatsAvailable() {
        course.setAvailableSeats(0);
        courseRepository.save(course);

        assertThatThrownBy(() -> service.validateSeatsAvailability(course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }

    @Test
    void shouldRejectWhenPrerequisiteNotCompleted() {
        course = TestUtils.createCourseWithPrerequisites("IFSP101", "Software Engineering", 4, List.of("IFSP201"));
        courseRepository.save(course);

        assertThatThrownBy(() -> service.validatePrerequisites(student, course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    void shouldRejectWhenScheduleConflictDetected() {
        Course c2 = TestUtils.createCourseWithSchedule("IFSP102", "Algorithms", 4,
                List.of(TestUtils.createClassSchedule("Monday", "11:00", "13:00")));
        course.setSchedule(List.of(TestUtils.createClassSchedule("Monday", "10:00", "12:00")));
        courseRepository.save(c2);
        enrollmentRepository.save(TestUtils.createEnrollment(student, c2, term));

        assertThatThrownBy(() -> service.validateScheduleConflict(course, enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected");
    }
}
