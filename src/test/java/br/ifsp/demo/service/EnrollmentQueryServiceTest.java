package br.ifsp.demo.service;

import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.model.Enrollment;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

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

    @ParameterizedTest(name = "Should Return Enrollment Details")
    @CsvSource({
            "12345,Math,08:00-10:00,4,30",
            "12345,Physics,10:00-12:00,3,25"
    })
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Should Return Enrollment Details When Student Has Active Courses")
    void shouldReturnEnrollmentDetailsWhenStudentHasActiveCourses(Long studentId, String courseName, String schedule, int credits, int vacancies) {
        when(repository.existsByStudentId(studentId)).thenReturn(true);
        when(repository.findByStudentId(studentId)).thenReturn(List.of(
                new Enrollment("Math", "08:00-10:00", 4, 30),
                new Enrollment("Physics", "10:00-12:00", 3, 25)
        ));

        List<Enrollment> enrollments = service.getEnrollmentsByStudent(studentId);

        assertNotNull(enrollments);
        assertFalse(enrollments.isEmpty(), "Enrollments list should not be empty");

        boolean found = enrollments.stream().anyMatch(e ->
                e.getCourseName().equals(courseName) &&
                        e.getSchedule().equals(schedule) &&
                        e.getCredits() == credits &&
                        e.getVacancies() == vacancies
        );

        assertTrue(found, "Expected enrollment details were not found");

        verify(repository).existsByStudentId(studentId);
        verify(repository).findByStudentId(studentId);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Should Throw NoCoursesFoundException When Student Has Enrollment But No Courses")
    void shouldThrowNoCoursesFoundExceptionWhenStudentHasEnrollmentButNoCourses() {
        Long studentId = 1112L;
        when(repository.existsByStudentId(studentId)).thenReturn(true);
        when(repository.findByStudentId(studentId)).thenReturn(List.of());

        NoCoursesFoundException exception = assertThrows(
                NoCoursesFoundException.class,
                () -> service.getEnrollmentsByStudent(studentId)
        );

        assertEquals("Nenhuma disciplina encontrada para esta matrícula.", exception.getMessage());

        verify(repository).existsByStudentId(studentId);
        verify(repository).findByStudentId(studentId);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Should Throw EnrollmentNotFoundException When StudentHas No Active Enrollment")
    void shouldThrowEnrollmentNotFoundExceptionWhenStudentHasNoActiveEnrollment() {
        Long studentId = 99999L;
        when(repository.existsByStudentId(studentId)).thenReturn(false);

        EnrollmentNotFoundException exception = assertThrows(
                EnrollmentNotFoundException.class,
                () -> service.getEnrollmentsByStudent(studentId)
        );

        assertEquals("Matrícula não encontrada ou inativa", exception.getMessage());

        verify(repository).existsByStudentId(studentId);
        verify(repository, never()).findByStudentId(anyLong());
    }

    @Test
    @Tag("FunctionalTest")
    @DisplayName("Should Return Real Enrollment Data")
    void functionalShouldReturnRealEnrollmentData() {
        EnrollmentRepository realRepository = new EnrollmentRepository() {
            @Override
            public boolean existsByStudentId(Long studentId) {
                return studentId.equals(12345L);
            }

            @Override
            public List<Enrollment> findByStudentId(Long studentId) {
                if (studentId.equals(12345L)) {
                    return List.of(
                            new Enrollment("Math", "08:00-10:00", 4, 30),
                            new Enrollment("Physics", "10:00-12:00", 3, 25)
                    );
                }
                return List.of();
            }
        };

        EnrollmentQueryService realService = new EnrollmentQueryService(realRepository);
        List<Enrollment> enrollments = realService.getEnrollmentsByStudent(12345L);

        assertEquals(2, enrollments.size());
        assertEquals("Math", enrollments.getFirst().getCourseName());
    }
}
