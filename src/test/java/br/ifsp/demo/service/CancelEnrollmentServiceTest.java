package br.ifsp.demo.service;

import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

class CancelEnrollmentServiceTest {

    @Mock
    private EnrollmentRepository repository;

    private CancelEnrollmentService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CancelEnrollmentService(repository);
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Cancel Existing Enrollment")
    void shouldCancelExistingEnrollment() {
        Long enrollmentId = 1L;
        Enrollment enrollment = new Enrollment(enrollmentId);

        when(repository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(repository.save(any(Enrollment.class))).thenReturn(enrollment);

        boolean result = service.cancelEnrollment(enrollmentId);

        assertTrue(result, "Enrollment canceled successfully");
        assertTrue(enrollment.isCanceled());
        verify(repository).findById(enrollmentId);
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Fail When Enrollment Does Not Exist")
    void shouldFailWhenEnrollmentDoesNotExist() {
        Long enrollmentId = 999L;

        when(repository.findById(enrollmentId)).thenReturn(Optional.empty());

        boolean result = service.cancelEnrollment(enrollmentId);

        assertFalse(result, "Enrollment was not found");
        verify(repository).findById(enrollmentId);
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Throw NullPointerException When Enrollment ID is Null")
    void shouldThrowNullExceptionWhenEnrollmentIdIsNull() {
        assertThatThrownBy(() -> service.cancelEnrollment(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ID cannot be null");
        verify(repository, never()).findById(any());
    }

    @Tag("TDD")
    @Tag("UnitTest")
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

    @Tag("TDD")
    @Tag("UnitTest")
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
    private EnrollmentRepository jpaRepository;

    @Autowired
    private CancelEnrollmentService realService;

    @AfterAll
    void clearDatabase() {
        jpaRepository.deleteAll();
    }

    @BeforeEach
    void setupFunctional() {
        jpaRepository.deleteAll();
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Persist Cancellation in Database")
    void shouldPersistCancellationInDatabase() {
        Enrollment enrollment = new Enrollment();
        enrollment.setDeadline(LocalDate.now().plusDays(1));
        jpaRepository.save(enrollment);

        boolean result = realService.cancelEnrollment(enrollment.getId());

        assertTrue(result);
        Enrollment updated = jpaRepository.findById(enrollment.getId()).orElseThrow();
        assertTrue(updated.isCanceled());
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Fail When Enrollment Not Found in Database")
    void shouldFailWhenEnrollmentNotFoundInDatabase() {
        Long enrollmentId = 999L;

        boolean result = realService.cancelEnrollment(enrollmentId);

        assertFalse(result);
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Throw Exception When Enrollment Already Cancelled")
    void shouldThrowExceptionWhenAlreadyCancelled() {
        Enrollment enrollment = new Enrollment();
        enrollment.setDeadline(LocalDate.now().plusDays(1));
        enrollment.cancel();
        jpaRepository.save(enrollment);

        assertThatThrownBy(() -> realService.cancelEnrollment(enrollment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Enrollment Is Already Cancelled");

        Enrollment updated = jpaRepository.findById(enrollment.getId()).orElseThrow();
        assertTrue(updated.isCanceled());
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Fail Cancellation When Deadline Has Expired")
    void shouldFailWhenCancellationWhenDeadlineExpired() {
        Enrollment enrollment = new Enrollment();
        enrollment.setDeadline(LocalDate.now().minusDays(1));
        jpaRepository.save(enrollment);

        assertThatThrownBy(() -> realService.cancelEnrollment(enrollment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cancellation Deadline has Expired");

        Enrollment updated = jpaRepository.findById(enrollment.getId()).orElseThrow();
        assertFalse(updated.isCanceled());
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Throw Exception When ID is Null")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> realService.cancelEnrollment(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ID cannot be null");
    }
}
