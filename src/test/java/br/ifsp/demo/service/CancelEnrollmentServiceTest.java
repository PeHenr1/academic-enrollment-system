package br.ifsp.demo.service;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import br.ifsp.demo.repository.StudentRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("UnitTest")
@Tag("TDD")
class CancelEnrollmentServiceTest {

    @Mock
    private EnrollmentRepository repository;

    private CancelEnrollmentService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CancelEnrollmentService(repository);
    }

    @Test
    @DisplayName("Should Cancel Existing Enrollment")
    void shouldCancelExistingEnrollment() {
        Long enrollmentId = 1L;

        Student student = new Student("123", "John Doe");
        Course course = new Course("IFSP101", "Software Engineering", 4);
        Term term = new Term(2025, 1);

        Enrollment enrollment = new Enrollment(student, course, term);
        ReflectionTestUtils.setField(enrollment, "id", enrollmentId);

        when(repository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(repository.save(any(Enrollment.class))).thenReturn(enrollment);

        boolean result = service.cancelEnrollment(enrollmentId);

        assertTrue(result, "Enrollment canceled successfully");
        assertTrue(enrollment.isCanceled());
        verify(repository).findById(enrollmentId);
    }

    @Test
    @DisplayName("Should Fail When Enrollment Does Not Exist")
    void shouldFailWhenEnrollmentDoesNotExist() {
        Long enrollmentId = 999L;

        when(repository.findById(enrollmentId)).thenReturn(Optional.empty());

        boolean result = service.cancelEnrollment(enrollmentId);

        assertFalse(result, "Enrollment was not found");
        verify(repository).findById(enrollmentId);
    }

    @Test
    @DisplayName("Should Throw NullPointerException When Enrollment ID is Null")
    void shouldThrowNullExceptionWhenEnrollmentIdIsNull() {
        assertThatThrownBy(() -> service.cancelEnrollment(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ID cannot be null");
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("Should Fail Cancellation When Deadline Has Expired")
    void shouldFailCancellationWhenDeadlineHasExpired() {
        Long enrollmentId = 5L;
        Enrollment enrollment = mock(Enrollment.class);

        when(repository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(enrollment.getCancellationDeadline()).thenReturn(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.cancelEnrollment(enrollmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cancellation Deadline has Expired");
        verify(repository).findById(enrollmentId);
    }

    @Test
    @DisplayName("Should Fail Cancellation When Enrollment Is Already Canceled")
    void shouldFailCancellationWhenEnrollmentIsAlreadyCanceled() {
        Long enrollmentId = 10L;
        Enrollment enrollment = mock(Enrollment.class);

        when(repository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(enrollment.isCanceled()).thenReturn(true);

        assertThatThrownBy(() -> service.cancelEnrollment(enrollmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Enrollment Is Already Cancelled");

        verify(repository).findById(enrollmentId);
    }
}

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CancelEnrollmentServiceFunctionalTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CancelEnrollmentService cancelEnrollmentService;

    @BeforeEach
    void cleanDatabase() {
        enrollmentRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @AfterAll
    void cleanup() {
        enrollmentRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("Should Persist Cancellation in Database")
    void shouldPersistCancellationInDatabase() {
        Student student = new Student("123", "John Doe");
        studentRepository.save(student);

        Course course = new Course("IFSP101", "Software Engineering", 4);
        courseRepository.save(course);

        Term term = new Term(2025, 1);
        Enrollment enrollment = new Enrollment(student, course, term);
        enrollment.setDeadline(LocalDate.now().plusDays(1));
        enrollmentRepository.save(enrollment);

        boolean result = cancelEnrollmentService.cancelEnrollment(enrollment.getId());

        assertTrue(result);
        Enrollment updated = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertTrue(updated.isCanceled());
    }

    @Test
    @DisplayName("Should Fail When Enrollment Not Found in Database")
    void shouldFailWhenEnrollmentNotFound() {
        boolean result = cancelEnrollmentService.cancelEnrollment(999L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should Throw Exception When Enrollment Already Cancelled")
    void shouldThrowExceptionWhenAlreadyCancelled() {
        Student student = new Student("124", "Jane Doe");
        studentRepository.save(student);

        Course course = new Course("IFSP102", "Databases", 4);
        courseRepository.save(course);

        Term term = new Term(2025, 1);
        Enrollment enrollment = new Enrollment(student, course, term);
        enrollment.setDeadline(LocalDate.now().plusDays(1));
        enrollment.cancel();
        enrollmentRepository.save(enrollment);

        assertThatThrownBy(() -> cancelEnrollmentService.cancelEnrollment(enrollment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Enrollment Is Already Cancelled");

        Enrollment updated = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertTrue(updated.isCanceled());
    }

    @Test
    @DisplayName("Should Fail Cancellation When Deadline Has Expired")
    void shouldFailWhenDeadlineExpired() {
        Student student = new Student("125", "Mark Smith");
        studentRepository.save(student);

        Course course = new Course("IFSP103", "Algorithms", 4);
        courseRepository.save(course);

        Term term = new Term(2025, 1);
        Enrollment enrollment = new Enrollment(student, course, term);
        enrollment.setDeadline(LocalDate.now().minusDays(1));
        enrollmentRepository.save(enrollment);

        assertThatThrownBy(() -> cancelEnrollmentService.cancelEnrollment(enrollment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cancellation Deadline has Expired");

        Enrollment updated = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertFalse(updated.isCanceled());
    }

    @Test
    @DisplayName("Should Throw Exception When ID is Null")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> cancelEnrollmentService.cancelEnrollment(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ID cannot be null");
    }
}
