package br.ifsp.demo.service;

import br.ifsp.demo.domain.*;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@Tag("UnitTest")
@Tag("TDD")
class EnrollStudentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollStudentService enrollStudentService;

    private Student student;
    private OfferedCourse offeredCourse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        student = new Student("123", "John Doe");
        offeredCourse = new OfferedCourse("IFSP101", "Software Engineering", 4);
    }

    @Test
    void shouldEnrollStudentSuccessfullyWhenAllRulesAreSatisfied() {
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(offeredCourse));
        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), offeredCourse.getTerm()))
                .thenReturn(List.of());
        offeredCourse.setAvailableSeats(5);

        enrollStudentService.enroll(student, List.of("IFSP101"));

        verify(enrollmentRepository).saveEnrollment(student, offeredCourse);
    }

    @Test
    void shouldThrowExceptionWhenPrerequisiteNotCompleted() {
        offeredCourse.setPrerequisites(List.of("IFSP201"));
        student.setCompletedCourses(List.of("IFSP101"));
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(offeredCourse));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of("IFSP101")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    void shouldThrowExceptionWhenCourseAlreadyCompleted() {
        student.setCompletedCourses(List.of("IFSP101"));
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(offeredCourse));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of("IFSP101")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Course already completed");
    }

    @Test
    void shouldThrowExceptionWhenExceedingMaxCredits() {
        offeredCourse = new OfferedCourse("IFSP101", "Software Engineering", 10);
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(offeredCourse));
        when(enrollmentRepository.calculateTotalCredits(student.getId(), offeredCourse.getTerm())).thenReturn(15);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of("IFSP101")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximum of 20 credits exceeded");
    }

    @Test
    void shouldThrowExceptionWhenScheduleConflicts() {
        ClassSchedule schedule = new ClassSchedule("Monday", "10:00", "12:00");
        offeredCourse.setSchedule(List.of(schedule));

        OfferedCourse alreadyEnrolled = new OfferedCourse("IFSP102", "Algorithms", 4);
        alreadyEnrolled.setSchedule(List.of(new ClassSchedule("Monday", "11:00", "13:00")));

        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(offeredCourse));
        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), offeredCourse.getTerm()))
                .thenReturn(List.of(alreadyEnrolled));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, List.of("IFSP101")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected");
    }

}
