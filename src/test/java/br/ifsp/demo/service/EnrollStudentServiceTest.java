package br.ifsp.demo.service;

import br.ifsp.demo.domain.*;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import br.ifsp.demo.repository.StudentRepository;
import br.ifsp.demo.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.Collections;
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
        when(student.getId()).thenReturn("SP123");

        term = Term.current();

        course = mock(Course.class);
        when(course.getCode()).thenReturn(courseCode);
        when(course.getPrerequisites()).thenReturn(List.of("IFSP100"));
        when(course.getAvailableSeats()).thenReturn(10);

        when(courseRepository.findByCode(courseCode)).thenReturn(Optional.of(course));

        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(anyString(), any(Term.class)))
                .thenReturn(new ArrayList<>());

        doNothing().when(validationService).validateCourseExists(course);
        doNothing().when(validationService).validateCourseAlreadyCompleted(student, course);
        doNothing().when(validationService).validatePrerequisites(student, course);
        doNothing().when(validationService).validateCreditLimit(student, course, term, new ArrayList<>());
        doNothing().when(validationService).validateScheduleConflict(course, new ArrayList<>());
        doNothing().when(validationService).validateSeatsAvailability(course);
    }

    @Test
    @DisplayName("Should Throw When Prerequisite Not Completed")
    void shouldThrowWhenPrerequisiteNotCompleted() {
        List<String> courseCodes = List.of(courseCode);
        doThrow(new BusinessRuleException("Missing prerequisite"))
                .when(validationService).validatePrerequisites(student, course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    @DisplayName("Should Throw When Course Already Completed")
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
    @DisplayName("Should Throw When No Seats Available")
    void shouldThrowWhenNoSeatsAvailable() {
        List<String> courseCodes = List.of(courseCode);
        doThrow(new BusinessRuleException("No seats available"))
                .when(validationService).validateSeatsAvailability(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }

    @Test
    @DisplayName("Should successfully enroll student and decrease seat (Kills L52 mutant)")
    void shouldEnrollStudentSuccessfullyAndDecreaseSeat() {
        List<String> courseCodes = List.of(courseCode);

        enrollStudentService.enroll(student, courseCodes, term);

        verify(validationService).validateCourseExists(course);
        verify(validationService).validateCourseAlreadyCompleted(student, course);
        verify(validationService).validatePrerequisites(student, course);
        verify(validationService).validateCreditLimit(student, course, term, new ArrayList<>());
        verify(validationService).validateScheduleConflict(course, new ArrayList<>());
        verify(validationService).validateSeatsAvailability(course);

        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(course).decreaseSeat();
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should throw when course list is null (Kills L30 mutant)")
    void shouldThrowWhenCourseListIsNull() {
        assertThatThrownBy(() -> enrollStudentService.enroll(student, null, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No courses provided for enrollment");
    }

    @Test
    @DisplayName("Should throw when course list is empty (Kills L30 mutant)")
    void shouldThrowWhenCourseListIsEmpty() {
        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No courses provided for enrollment");
    }

    @Test
    @DisplayName("Should throw when course code is not found (Kills L36 mutant)")
    void shouldThrowWhenCourseCodeNotFound() {
        String invalidCode = "INVALID999";
        when(courseRepository.findByCode(invalidCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(invalidCode), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course not found: " + invalidCode);
    }

    @Test
    @DisplayName("Should throw when validateCourseExists fails (Kills L38 mutant)")
    void shouldThrowWhenValidateCourseExistsFails() {
        List<String> courseCodes = List.of(courseCode);
        doThrow(new BusinessRuleException("Course does not exist"))
                .when(validationService).validateCourseExists(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course does not exist");
    }

    @Test
    @DisplayName("Should throw when credit limit is exceeded (Kills L45 mutant)")
    void shouldThrowWhenCreditLimitExceeded() {
        List<String> courseCodes = List.of(courseCode);
        doThrow(new BusinessRuleException("Maximum credits exceeded"))
                .when(validationService).validateCreditLimit(any(), any(), any(), any());

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Maximum credits exceeded");
    }

    @Test
    @DisplayName("Should throw when schedule conflicts")
    void shouldThrowWhenScheduleConflicts() {
        List<String> courseCodes = List.of(courseCode);
        doThrow(new BusinessRuleException("Schedule conflict"))
                .when(validationService).validateScheduleConflict(any(), any());

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Schedule conflict");
    }
}

@Tag("UnitTest")
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
    @DisplayName("Should Enroll Student Successfully")
    void shouldEnrollStudentSuccessfully() {
        enrollStudentService.enroll(student, List.of(course.getCode()), term);

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term);
        assertThat(enrollments).hasSize(1);
        assertThat(enrollments.get(0).getCourse().getCode()).isEqualTo(course.getCode());
    }

    @Test
    @DisplayName("Should Throw When No Seats Available")
    void shouldThrowWhenNoSeatsAvailable() {
        course.setAvailableSeats(0);
        courseRepository.save(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(course.getCode()), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }

    @Test
    @DisplayName("Should Throw When Prerequisite Not Completed")
    void shouldThrowWhenPrerequisiteNotCompleted() {
        course.setPrerequisites(new ArrayList<>(List.of("IFSP201")));
        courseRepository.save(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(course.getCode()), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    @DisplayName("Should Throw When Schedule Conflicts")
    void shouldThrowWhenScheduleConflicts() {
        Course c1 = TestUtils.createCourse("IFSP102", "Algorithms", 4);
        c1.setSchedule(new ArrayList<>(List.of(new ClassSchedule("Monday", "11:00", "13:00"))));
        c1.setAvailableSeats(10);
        c1 = courseRepository.save(c1);

        course.setSchedule(new ArrayList<>(List.of(new ClassSchedule("Monday", "10:00", "12:00"))));
        courseRepository.save(course);

        enrollmentRepository.save(new Enrollment(student, c1, term));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of(course.getCode()), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected");
    }
}

@Tag("UnitTest")
@Tag("Structural")
class EnrollStudentServiceStructuralTest {

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        student = mock(Student.class);
        when(student.getId()).thenReturn("5L");
        when(student.getCompletedCourses()).thenReturn(new ArrayList<>());

        term = Term.current();

        String courseCode1 = "IFSP101";
        Course course1 = TestUtils.createCourse(courseCode1, "Intro to Programming", 4);
        course1.setId(1L);
        course1.setAvailableSeats(10);

        String courseCode2 = "IFSP102";
        Course course2 = TestUtils.createCourse(courseCode2, "Data Structures", 6);
        course2.setId(2L);
        course2.setAvailableSeats(5);

        when(courseRepository.findByCode(courseCode1)).thenReturn(Optional.of(course1));
        when(courseRepository.findByCode(courseCode2)).thenReturn(Optional.of(course2));

        doNothing().when(validationService).validateCourseExists(any(Course.class));
        doNothing().when(validationService).validateCourseAlreadyCompleted(any(Student.class), any(Course.class));
        doNothing().when(validationService).validatePrerequisites(any(Student.class), any(Course.class));
        doNothing().when(validationService).validateCreditLimit(any(Student.class), any(Course.class), any(Term.class), anyList());
        doNothing().when(validationService).validateScheduleConflict(any(Course.class), anyList());
        doNothing().when(validationService).validateSeatsAvailability(any(Course.class));

        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(anyString(), any(Term.class)))
                .thenReturn(new ArrayList<>());
    }

    @Test
    @DisplayName("Should Throw When Course List Is Null")
    void shouldThrowWhenCourseListIsNull() {
        assertThatThrownBy(() -> enrollStudentService.enroll(student, null, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No courses provided for enrollment");

        verifyNoInteractions(courseRepository, enrollmentRepository);
    }

    @Test
    @DisplayName("Should Throw When Course List Is Empty")
    void shouldThrowWhenCourseListIsEmpty() {
        assertThatThrownBy(() -> enrollStudentService.enroll(student, Collections.emptyList(), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No courses provided for enrollment");

        verifyNoInteractions(courseRepository, enrollmentRepository);
    }

    @Test
    @DisplayName("Should Throw When Course Is Not Found")
    void shouldThrowWhenCourseIsNotFound() {
        String nonExistentCode = "IFSP404";
        when(courseRepository.findByCode(nonExistentCode)).thenReturn(Optional.empty());
        List<String> courseCodes = List.of(nonExistentCode);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Course not found: " + nonExistentCode);

        verify(courseRepository, times(1)).findByCode(nonExistentCode);
        verify(enrollmentRepository, never()).save(any());
    }
}

@Tag("UnitTest")
@Tag("Mutation")
class EnrollStudentServiceMutationTest {

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
    private Course course1;
    private final String courseCode1 = "IFSP101";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        student = mock(Student.class);
        when(student.getId()).thenReturn("5L");
        when(student.getCompletedCourses()).thenReturn(new ArrayList<>());

        term = Term.current();

        course1 = mock(Course.class);
        when(course1.getCode()).thenReturn(courseCode1);
        when(course1.getAvailableSeats()).thenReturn(10);

        when(courseRepository.findByCode(courseCode1)).thenReturn(Optional.of(course1));

        doNothing().when(validationService).validateCourseExists(any(Course.class));
        doNothing().when(validationService).validateCourseAlreadyCompleted(any(Student.class), any(Course.class));
        doNothing().when(validationService).validatePrerequisites(any(Student.class), any(Course.class));
        doNothing().when(validationService).validateCreditLimit(any(Student.class), any(Course.class), any(Term.class), anyList());
        doNothing().when(validationService).validateScheduleConflict(any(Course.class), anyList());
        doNothing().when(validationService).validateSeatsAvailability(any(Course.class));

        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(anyString(), any(Term.class)))
                .thenReturn(new ArrayList<>());
    }

    @Test
    @DisplayName("Should Fail If Course Exists Validation Is Skipped")
    void shouldFailIfCourseExistsValidationIsSkipped() {
        List<String> courseCodes = List.of(courseCode1);

        doThrow(new BusinessRuleException("Course must exist")).when(validationService).validateCourseExists(course1);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Course must exist");

        verify(validationService, times(1)).validateCourseExists(course1);
        verify(validationService, never()).validateCourseAlreadyCompleted(any(), any());
    }

    @Test
    @DisplayName("Should Throw When Credit Limit Is Exceeded")
    void shouldThrowWhenCreditLimitIsExceeded() {
        List<String> courseCodes = List.of(courseCode1);

        doThrow(new BusinessRuleException("Credit limit exceeded"))
                .when(validationService).validateCreditLimit(eq(student), eq(course1), eq(term), anyList());

        assertThatThrownBy(() -> enrollStudentService.enroll(student, courseCodes, term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Credit limit exceeded");

        verify(enrollmentRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Ensure Seat Is Decreased")
    void shouldEnsureSeatIsDecreased() {
        List<String> courseCodes = List.of(courseCode1);

        enrollStudentService.enroll(student, courseCodes, term);

        verify(course1, times(1)).decreaseSeat();

        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        verify(courseRepository, times(1)).save(course1);
    }
}