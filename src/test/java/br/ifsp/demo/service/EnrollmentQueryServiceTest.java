package br.ifsp.demo.service;

import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.model.Course;
import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class EnrollmentQueryServiceTest {

    private EnrollmentRepository enrollmentRepository;
    private CourseRepository courseRepository;
    private EnrollmentQueryService service;

    @BeforeEach
    void setup() {
        enrollmentRepository = mock(EnrollmentRepository.class);
        courseRepository = mock(CourseRepository.class);
        service = new EnrollmentQueryService(enrollmentRepository, courseRepository);
    }

    @Test
    @DisplayName("Should Return Courses for Enrollment")
    void shouldReturnCoursesForEnrollment() {
        Long enrollmentId = 1L;
        Course math = new Course("Math", "08:00-10:00", 4, 30, null);
        Course physics = new Course("Physics", "10:00-12:00", 3, 25, null);

        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(true);
        when(courseRepository.findByEnrollmentId(enrollmentId)).thenReturn(List.of(math, physics));

        List<Course> result = service.getCoursesByEnrollment(enrollmentId);

        assertThat(result).hasSize(2)
                .extracting(Course::getCourseName)
                .containsExactlyInAnyOrder("Math", "Physics");

        verify(enrollmentRepository).existsById(enrollmentId);
        verify(courseRepository).findByEnrollmentId(enrollmentId);
    }

    @Test
    @DisplayName("Should Throw NoCoursesFoundException When No Courses")
    void shouldThrowNoCoursesFoundException() {
        Long enrollmentId = 2L;
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(true);
        when(courseRepository.findByEnrollmentId(enrollmentId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getCoursesByEnrollment(enrollmentId))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("Nenhuma disciplina encontrada para esta matrícula.");
    }

    @Test
    @DisplayName("Should Throw EnrollmentNotFoundException When Enrollment Does Not Exist")
    void shouldThrowEnrollmentNotFoundException() {
        Long enrollmentId = 999L;
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(false);

        assertThatThrownBy(() -> service.getCoursesByEnrollment(enrollmentId))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Matrícula não encontrada ou inativa");
    }
}

@SpringBootTest
class EnrollmentQueryServiceFunctionalTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentQueryService service;

    @BeforeEach
    void setup() {
        courseRepository.deleteAll();
        enrollmentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should Return Multiple Courses for Enrollment")
    void shouldReturnMultipleCourses() {
        var enrollment = enrollmentRepository.save(new Enrollment());
        Course math = new Course("Math", "08:00-10:00", 4, 30, enrollment);
        Course physics = new Course("Physics", "10:00-12:00", 3, 25, enrollment);
        courseRepository.saveAll(List.of(math, physics));

        List<Course> result = service.getCoursesByEnrollment(enrollment.getId());

        assertThat(result).hasSize(2)
                .extracting(Course::getCourseName)
                .containsExactlyInAnyOrder("Math", "Physics");
    }

    @Test
    @DisplayName("Should Throw NoCoursesFoundException When Enrollment Has No Courses")
    void shouldThrowNoCoursesFoundException() {
        var enrollment = enrollmentRepository.save(new Enrollment());

        assertThatThrownBy(() -> service.getCoursesByEnrollment(enrollment.getId()))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("Nenhuma disciplina encontrada para esta matrícula.");
    }

    @Test
    @DisplayName("Should Throw EnrollmentNotFoundException When Enrollment Does Not Exist")
    void shouldThrowEnrollmentNotFoundException() {
        assertThatThrownBy(() -> service.getCoursesByEnrollment(999L))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Matrícula não encontrada ou inativa");
    }
}