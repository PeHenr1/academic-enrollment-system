package br.ifsp.demo.service;

import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class EnrollmentQueryServiceTest {

    private EnrollmentRepository repository;
    private EnrollmentQueryService service;

    @BeforeEach
    void setup() {
        repository = mock(EnrollmentRepository.class);
        service = new EnrollmentQueryService(repository);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Should Return Enrollment When Student Has Active Enrollment")
    void shouldReturnEnrollmentWhenStudentHasActiveEnrollment() {
        Long id = 1L;
        Enrollment enrollment = new Enrollment("Math", "08:00-10:00", 4, 30);
        when(repository.existsById(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(Optional.of(enrollment));

        Optional<Enrollment> result = service.getEnrollmentsByStudent(id);

        assertThat(result).isPresent();
        assertThat(result.get().getCourseName()).isEqualTo("Math");

        verify(repository).existsById(id);
        verify(repository).findById(id);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Should Throw NoCoursesFoundException When Enrollment Found But No Courses")
    void shouldThrowNoCoursesFoundExceptionWhenEnrollmentHasNoCourses() {
        Long studentId = 2L;
        when(repository.existsById(studentId)).thenReturn(true);
        when(repository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEnrollmentsByStudent(studentId))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("Nenhuma disciplina encontrada para esta matrícula.");

        verify(repository).existsById(studentId);
        verify(repository).findById(studentId);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Should Throw EnrollmentNotFoundException When Student Has No Enrollment")
    void shouldThrowEnrollmentNotFoundExceptionWhenStudentHasNoEnrollment() {
        Long studentId = 999L;
        when(repository.existsById(studentId)).thenReturn(false);

        assertThatThrownBy(() -> service.getEnrollmentsByStudent(studentId))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Matrícula não encontrada ou inativa");

        verify(repository).existsById(studentId);
        verify(repository, never()).findById(anyLong());
    }
}

@SpringBootTest
class EnrollmentQueryServiceFunctionalTest {

    @Autowired
    private EnrollmentRepository jpaRepository;

    @Autowired
    private EnrollmentQueryService realService;

    @BeforeEach
    void setupFunctional() {
        jpaRepository.deleteAll();
    }

    @Test
    @Tag("Functional")
    @DisplayName("Functional: Should Return Enrollment for Active Student")
    void shouldReturnEnrollmentForActiveStudent() {
        Enrollment enrollment = new Enrollment("Math", "08:00-10:00", 4, 30);
        enrollment.setId(1L);
        jpaRepository.save(enrollment);

        Optional<Enrollment> result = realService.getEnrollmentsByStudent(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCourseName()).isEqualTo("Math");
    }

    @Test
    @Tag("Functional")
    @DisplayName("Functional: Should Throw NoCoursesFoundException When Enrollment Has No Courses")
    void shouldThrowNoCoursesFoundExceptionWhenEnrollmentHasNoCourses() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        jpaRepository.save(enrollment);

        assertThatThrownBy(() -> realService.getEnrollmentsByStudent(1L))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("Nenhuma disciplina encontrada para esta matrícula.");
    }
}