package br.ifsp.demo.service;

import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Tag("UnitTest")
@Tag("TDD")
class CancelEnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    private CancelEnrollmentService service;

    private final String studentId = "student-owner-id";
    private Student mockStudent;
    private Enrollment mockEnrollment;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CancelEnrollmentService(enrollmentRepository, courseRepository);

        mockStudent = mock(Student.class);
        mockEnrollment = mock(Enrollment.class);

        when(mockStudent.getId()).thenReturn(studentId);
        when(mockEnrollment.getStudent()).thenReturn(mockStudent);
        when(mockEnrollment.getCancellationDeadline()).thenReturn(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("Should Cancel Existing Enrollment and Increase Seat")
    void shouldCancelExistingEnrollment() {
        Long enrollmentId = 1L;
        Course mockCourse = mock(Course.class);

        when(mockEnrollment.getCourse()).thenReturn(mockCourse);
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(mockEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(mockEnrollment);

        boolean result = service.cancelEnrollment(enrollmentId, studentId);

        assertTrue(result);
        verify(mockEnrollment).cancel();
        verify(mockCourse).increaseSeat();
        verify(courseRepository).save(mockCourse);
    }

    @Test
    @DisplayName("Should Cancel Enrollment Even If Course Is Null")
    void shouldCancelEnrollmentEvenIfCourseIsNull() {
        Long enrollmentId = 1L;

        when(mockEnrollment.getCourse()).thenReturn(null);

         when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(mockEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(mockEnrollment);

        boolean result = service.cancelEnrollment(enrollmentId, studentId);

        assertTrue(result);
        verify(mockEnrollment).cancel();
        verify(enrollmentRepository).save(mockEnrollment);

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should Throw AccessDeniedException When Student ID Does Not Match")
    void shouldThrowAccessDeniedWhenStudentIdDoesNotMatch() {
        Long enrollmentId = 1L;
        String attackerStudentId = "attacker-id";

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(mockEnrollment));

        assertThatThrownBy(() -> service.cancelEnrollment(enrollmentId, attackerStudentId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to cancel this enrollment.");

        verify(enrollmentRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Fail When Enrollment Does Not Exist")
    void shouldFailWhenEnrollmentDoesNotExist() {
        Long enrollmentId = 999L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        boolean result = service.cancelEnrollment(enrollmentId, studentId);

        assertFalse(result, "Enrollment was not found");
        verify(enrollmentRepository).findById(enrollmentId);
    }

    @Test
    @DisplayName("Should Throw NullPointerException When Enrollment ID is Null")
    void shouldThrowNullExceptionWhenEnrollmentIdIsNull() {
        assertThatThrownBy(() -> service.cancelEnrollment(null, studentId))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ID cannot be null");
        verify(enrollmentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should Fail Cancellation When Deadline Has Expired")
    void shouldFailCancellationWhenDeadlineHasExpired() {
        Long enrollmentId = 5L;
        when(mockEnrollment.getCancellationDeadline()).thenReturn(LocalDate.now().minusDays(1));
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(mockEnrollment));

        assertThatThrownBy(() -> service.cancelEnrollment(enrollmentId, studentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cancellation Deadline has Expired");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Fail Cancellation When Enrollment Is Already Canceled")
    void shouldFailCancellationWhenEnrollmentIsAlreadyCanceled() {
        Long enrollmentId = 10L;
        when(mockEnrollment.isCanceled()).thenReturn(true);
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(mockEnrollment));

        assertThatThrownBy(() -> service.cancelEnrollment(enrollmentId, studentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Enrollment Is Already Cancelled");

        verify(enrollmentRepository, never()).save(any());
    }
}

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
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
    @DisplayName("Should Persist Cancellation and Increase Seat in Database")
    void shouldPersistCancellationInDatabase() {
        Enrollment enrollment = TestUtils.createEnrollmentWithDeadline(LocalDate.now().plusDays(1));
        Student student = studentRepository.save(enrollment.getStudent());
        Course course = courseRepository.save(enrollment.getCourse());

        int initialSeats = course.getAvailableSeats();

        enrollment = enrollmentRepository.save(enrollment);

        boolean result = cancelEnrollmentService.cancelEnrollment(enrollment.getId(), student.getId());

        assertTrue(result);
        Enrollment updatedEnrollment = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();

        assertTrue(updatedEnrollment.isCanceled());
        assertEquals(initialSeats + 1, updatedCourse.getAvailableSeats());
    }

    @Test
    @DisplayName("Should Throw AccessDenied When Canceling Another Student's Enrollment")
    void shouldThrowAccessDeniedWhenCancelingAnotherStudentsEnrollment() {
        Student student1 = studentRepository.save(TestUtils.createStudent("student1-id", "Student One"));
        Course course1 = courseRepository.save(TestUtils.createCourse("C1", "Course 1", 4));
        Enrollment enrollment = enrollmentRepository.save(new Enrollment(student1, course1, Term.current()));

        Student student2 = studentRepository.save(TestUtils.createStudent("student2-id", "Student Two"));

        final Long enrollmentId = enrollment.getId();
        final String attackerId = student2.getId();

        assertThatThrownBy(() ->
                cancelEnrollmentService.cancelEnrollment(enrollmentId, attackerId)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to cancel this enrollment.");

        Enrollment notCanceled = enrollmentRepository.findById(enrollmentId).orElseThrow();
        assertFalse(notCanceled.isCanceled());
    }

    @Test
    @DisplayName("Should Fail When Enrollment Not Found in Database")
    void shouldFailWhenEnrollmentNotFound() {
        boolean result = cancelEnrollmentService.cancelEnrollment(999L, "any-student-id");
        assertFalse(result);
    }

    @Test
    @DisplayName("Should Throw Exception When Enrollment Already Cancelled")
    void shouldThrowExceptionWhenAlreadyCancelled() {
        Enrollment enrollment = TestUtils.createCanceledEnrollment();
        Student student = studentRepository.save(enrollment.getStudent());
        courseRepository.save(enrollment.getCourse());

        final Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        final String studentId = student.getId();

        assertThatThrownBy(() -> cancelEnrollmentService.cancelEnrollment(savedEnrollment.getId(), studentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Enrollment Is Already Cancelled");

        Enrollment updated = enrollmentRepository.findById(savedEnrollment.getId()).orElseThrow();
        assertTrue(updated.isCanceled());
    }

    @Test
    @DisplayName("Should Fail Cancellation When Deadline Has Expired")
    void shouldFailWhenDeadlineExpired() {
        Enrollment enrollment = TestUtils.createExpiredEnrollment();
        Student student = studentRepository.save(enrollment.getStudent());
        courseRepository.save(enrollment.getCourse());

        final Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        final String studentId = student.getId();

        assertThatThrownBy(() -> cancelEnrollmentService.cancelEnrollment(savedEnrollment.getId(), studentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cancellation Deadline has Expired");

        Enrollment updated = enrollmentRepository.findById(savedEnrollment.getId()).orElseThrow();
        assertFalse(updated.isCanceled());
    }

    @Test
    @DisplayName("Should Throw Exception When ID is Null")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> cancelEnrollmentService.cancelEnrollment(null, "any-student-id"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ID cannot be null");
    }
}