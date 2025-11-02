package br.ifsp.demo.service;

import br.ifsp.demo.domain.*;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import br.ifsp.demo.repository.StudentRepository;
import br.ifsp.demo.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class EnrollStudentServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentValidationService validationService;

    @InjectMocks
    private EnrollStudentService enrollStudentService;

    private Student student;
    private Term term;
    private Course course;
    private final String courseCode = "IFSP101";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        student = mock(Student.class);
        when(student.getCompletedCourses()).thenReturn(new ArrayList<>());

        term = Term.current();

        course = TestUtils.createCourse(courseCode, "Intro to Programming", 4);
        course.setId(1L);
        course.setPrerequisites(List.of("IFSP100"));
        course.setAvailableSeats(10);

        when(courseRepository.findByCode(courseCode)).thenReturn(Optional.of(course));

        doNothing().when(validationService).validateCourseExists(course);
        doNothing().when(validationService).validateCourseAlreadyCompleted(student, course);
        doNothing().when(validationService).validatePrerequisites(student, course);
        doNothing().when(validationService).validateCreditLimit(student, course, term, new ArrayList<>());
        doNothing().when(validationService).validateScheduleConflict(course, new ArrayList<>());
        doNothing().when(validationService).validateSeatsAvailability(course);
    }

    @Test
    void shouldThrowWhenPrerequisiteNotCompleted() {
        List<String> courseCodes = List.of(courseCode);
        doThrow(new BusinessRuleException("Missing prerequisite"))
                .when(validationService).validatePrerequisites(student, course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    void shouldThrowWhenCourseAlreadyCompleted() {
        List<String> courseCodes = List.of(courseCode);
        when(student.getCompletedCourses()).thenReturn(List.of(courseCode));

        doThrow(new BusinessRuleException("Course already completed"))
                .when(validationService).validateCourseAlreadyCompleted(student, course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Course already completed");
    }

    @Test
    void shouldThrowWhenNoSeatsAvailable() {
        List<String> courseCodes = List.of(courseCode);
        doThrow(new BusinessRuleException("No seats available"))
                .when(validationService).validateSeatsAvailability(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }
}


@Tag("Functional")
@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnrollStudentServiceFunctionalTest {

    @Autowired
    private EnrollStudentService enrollStudentService;

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

        student = TestUtils.createDefaultStudent();
        student.setCompletedCourses(new ArrayList<>());
        student = studentRepository.save(student);

        term = Term.current();

        course = TestUtils.createCourse("IFSP101", "Software Engineering", 4);
        course.setAvailableSeats(5);
        course.setSchedule(new ArrayList<>());
        course.setPrerequisites(new ArrayList<>());
        course = courseRepository.save(course);
    }

    @Test
    void shouldEnrollStudentSuccessfully() {
        enrollStudentService.enroll(student, List.of(course.getCode()), term);

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term);
        assertThat(enrollments).hasSize(1);
        assertThat(enrollments.get(0).getCourse().getCode()).isEqualTo(course.getCode());
    }

    @Test
    void shouldThrowWhenNoSeatsAvailable() {
        course.setAvailableSeats(0);
        courseRepository.save(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(course.getCode()), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }

    @Test
    void shouldThrowWhenPrerequisiteNotCompleted() {
        course.setPrerequisites(new ArrayList<>(List.of("IFSP201")));
        courseRepository.save(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(course.getCode()), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    void shouldThrowWhenScheduleConflicts() {
        Course c1 = TestUtils.createCourse("IFSP102", "Algorithms", 4);
        c1.setSchedule(new ArrayList<>(List.of(new ClassSchedule("Monday", "11:00", "13:00"))));
        c1 = courseRepository.save(c1);

        course.setSchedule(new ArrayList<>(List.of(new ClassSchedule("Monday", "10:00", "12:00"))));
        courseRepository.save(course);

        enrollmentRepository.save(new Enrollment(student, c1, term));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(course.getCode()), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected");
    }
}