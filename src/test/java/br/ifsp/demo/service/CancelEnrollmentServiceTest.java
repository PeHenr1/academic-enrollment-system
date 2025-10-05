package br.ifsp.demo.service;

import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Tag;

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
        Enrollment enrollment = mock(Enrollment.class);

        when(repository.findById(enrollmentId)).thenReturn(enrollment);
        when(enrollment.getCancellationDeadline()).thenReturn(LocalDate.now());
        when(enrollment.isCanceled()).thenReturn(false);

        boolean result = service.cancelEnrollment(enrollmentId);

        assertTrue(result, "Enrollment canceled successfully");
        verify(repository).findById(enrollmentId);
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Fail When Enrollment Does Not Exist")
    void shouldFailWhenEnrollmentDoesNotExist() {
        Long enrollmentId = 999L;

        when(repository.findById(enrollmentId)).thenReturn(null);

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

        when(repository.findById(enrollmentId)).thenReturn(enrollment);
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

        when(repository.findById(enrollmentId)).thenReturn(enrollment);
        when(enrollment.isCanceled()).thenReturn(true);

        assertThatThrownBy(() -> service.cancelEnrollment(enrollmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Enrollment Is Already Cancelled");

        verify(repository).findById(enrollmentId);
    }
}
