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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
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

@Tag("UnitTest")
@Tag("Structural")
class EnrollmentValidationServiceStructuralTest {

    private EnrollmentValidationService service;
    private Student student;
    private Course course;
    private Term term;

    @BeforeEach
    void setup() {
        service = new EnrollmentValidationService();
        student = TestUtils.createDefaultStudent();
        term = TestUtils.createDefaultTerm();

        course = TestUtils.createCourse("IFSP101", "Software Engineering", 4);
        course.setAvailableSeats(5);
        course.setSchedule(List.of(TestUtils.createClassSchedule("Monday", "10:00", "12:00")));
        course.setPrerequisites(List.of("IFSP001"));
    }

    @Test
    @DisplayName("Should Not Throw Exception When Seats Are Available")
    void shouldNotThrowExceptionWhenSeatsAreAvailable() {
        course.setAvailableSeats(1);
        assertThatCode(() -> service.validateSeatsAvailability(course))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCourseAlreadyCompleted() {
        student = TestUtils.createStudentWithCompletedCourses(student.getId(), student.getName(), List.of("IFSP101"));
        assertThatThrownBy(() -> service.validateCourseAlreadyCompleted(student, course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Course already completed");

        student.setCompletedCourses(new ArrayList<>());
        assertThatCode(() -> service.validateCourseAlreadyCompleted(student, course))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePrerequisites() {
        course.setPrerequisites(null);
        assertThatCode(() -> service.validatePrerequisites(student, course))
                .doesNotThrowAnyException();

        course.setPrerequisites(List.of());
        assertThatCode(() -> service.validatePrerequisites(student, course))
                .doesNotThrowAnyException();

        course.setPrerequisites(List.of("IFSP001", "IFSP002"));
        student.setCompletedCourses(List.of("IFSP002"));

        assertThatThrownBy(() -> service.validatePrerequisites(student, course))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite: IFSP001");

        student.setCompletedCourses(List.of("IFSP001", "IFSP002"));
        assertThatCode(() -> service.validatePrerequisites(student, course))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreditLimit() {
        int maxCredits = 20;
        Course existingCourse = TestUtils.createCourse("IFSP100", "Intro", 15);
        Enrollment existingEnrollment = TestUtils.createEnrollment(student, existingCourse, term);
        List<Enrollment> currentEnrollments = List.of(existingEnrollment);

        course.setCredits(6);
        assertThatThrownBy(() -> service.validateCreditLimit(student, course, term, currentEnrollments))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximum of " + maxCredits + " credits exceeded");

        course.setCredits(5);
        assertThatCode(() -> service.validateCreditLimit(student, course, term, currentEnrollments))
                .doesNotThrowAnyException();
    }

    @Test
    void validateScheduleConflict() {
        ClassSchedule schedule1 = TestUtils.createClassSchedule("Monday", "10:00", "12:00");
        ClassSchedule schedule2 = TestUtils.createClassSchedule("Monday", "11:00", "13:00");
        ClassSchedule schedule3 = TestUtils.createClassSchedule("Tuesday", "14:00", "16:00");

        course.setSchedule(List.of(schedule1));

        Course enrolledCourse = TestUtils.createCourseWithSchedule("IFSP102", "Algorithms", 4, List.of(schedule2));
        Enrollment existingEnrollment = TestUtils.createEnrollment(student, enrolledCourse, term);
        List<Enrollment> currentEnrollments = new ArrayList<>(List.of(existingEnrollment));

        assertThatThrownBy(() -> service.validateScheduleConflict(course, currentEnrollments))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected between IFSP102 and IFSP101");

        Course enrolledCourse2 = TestUtils.createCourseWithSchedule("IFSP103", "History", 30, List.of(schedule3));
        Enrollment existingEnrollment2 = TestUtils.createEnrollment(student, enrolledCourse2, term);
        currentEnrollments.clear();
        currentEnrollments.add(existingEnrollment2);

        assertThatCode(() -> service.validateScheduleConflict(course, currentEnrollments))
                .doesNotThrowAnyException();
    }

    @Test
    void validateScheduleConflict_EmptyEnrollments() {
        assertThatCode(() -> service.validateScheduleConflict(course, Collections.emptyList()))
                .doesNotThrowAnyException();
    }
}