package br.ifsp.demo.service;

import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import br.ifsp.demo.repository.StudentRepository;
import br.ifsp.demo.util.TestUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

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
        Enrollment enrollment = TestUtils.createEnrollmentWithId(enrollmentId);
        when(repository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(repository.save(any(Enrollment.class))).thenReturn(enrollment);

        boolean result = service.cancelEnrollment(enrollmentId);

        assertTrue(result);
        assertTrue(enrollment.isCanceled());
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
        Enrollment enrollment = TestUtils.createEnrollmentWithId(enrollmentId);
        enrollment.setDeadline(LocalDate.now().minusDays(1));

        when(repository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.cancelEnrollment(enrollmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cancellation Deadline has Expired");

        verify(repository).findById(enrollmentId);
    }

    @Test
    @DisplayName("Should Fail Cancellation When Enrollment Is Already Canceled")
    void shouldFailCancellationWhenEnrollmentIsAlreadyCanceled() {
        Long enrollmentId = 10L;
        Enrollment enrollment = TestUtils.createCanceledEnrollment();
        ReflectionTestUtils.setField(enrollment, "id", enrollmentId);

        when(repository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

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
        Enrollment enrollment = TestUtils.createEnrollmentWithDeadline(LocalDate.now().plusDays(1));
        studentRepository.save(enrollment.getStudent());
        courseRepository.save(enrollment.getCourse());
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
        Enrollment enrollment = TestUtils.createCanceledEnrollment();
        studentRepository.save(enrollment.getStudent());
        courseRepository.save(enrollment.getCourse());
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
        Enrollment enrollment = TestUtils.createExpiredEnrollment();
        studentRepository.save(enrollment.getStudent());
        courseRepository.save(enrollment.getCourse());
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
