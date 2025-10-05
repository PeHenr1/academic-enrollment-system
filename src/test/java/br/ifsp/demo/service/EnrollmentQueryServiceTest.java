package br.ifsp.demo.service;

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

    @Test
    @DisplayName("Should Return Enrollments For Student")
    void shouldReturnEnrollmentsForStudent() {
        Long studentId = 12345L;

        when(repository.findByStudentId(studentId)).thenReturn(List.of(
                new Enrollment("Math", "08:00-10:00", 4, 30)
        ));

        List<Enrollment> enrollments = service.getEnrollmentsByStudent(studentId);
        assertNotNull(enrollments);
        verify(repository, times(1)).findByStudentId(studentId);
    }

    @ParameterizedTest
    @CsvSource({
            "12345,Math,08:00-10:00,4,30",
            "12345,Physics,10:00-12:00,3,25"
    })
    void shouldContainEnrollmentDetails(Long studentId, String courseName, String schedule, int credits, int vacancies) {
        when(repository.findByStudentId(studentId)).thenReturn(List.of(
                new Enrollment("Math", "08:00-10:00", 4, 30),
                new Enrollment("Physics", "10:00-12:00", 3, 25)
        ));

        List<Enrollment> enrollments = service.getEnrollmentsByStudent(studentId);

        boolean found = enrollments.stream().anyMatch(e ->
                e.getCourseName().equals(courseName) &&
                        e.getSchedule().equals(schedule) &&
                        e.getCredits() == credits &&
                        e.getVacancies() == vacancies
        );

        assertTrue(found, "Enrollment details should be present");
        verify(repository, times(1)).findByStudentId(studentId);
    }
}
