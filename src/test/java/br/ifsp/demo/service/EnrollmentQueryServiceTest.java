package br.ifsp.demo.service;

import br.ifsp.demo.domain.*;
import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import br.ifsp.demo.repository.StudentRepository;
import br.ifsp.demo.util.TestUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class EnrollmentQueryServiceTest {

    private EnrollmentRepository enrollmentRepository;
    private CourseRepository courseRepository;
    private EnrollmentQueryService service;

    private final String studentId = "SP123";
    private final Term term = Term.current();
    private Enrollment mockEnrollment1;
    private Enrollment mockEnrollment2;

    @BeforeEach
    void setup() {
        enrollmentRepository = mock(EnrollmentRepository.class);
        courseRepository = mock(CourseRepository.class);
        service = new EnrollmentQueryService(enrollmentRepository, courseRepository);

        mockEnrollment1 = mock(Enrollment.class);
        mockEnrollment2 = mock(Enrollment.class);
    }

    @Test
    @DisplayName("Should Return Enrollments for Student")
    void shouldReturnEnrollmentsForStudent() {
        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(studentId, term))
                .thenReturn(List.of(mockEnrollment1, mockEnrollment2));

        List<Enrollment> result = service.getEnrollmentsByStudent(studentId, term);

        assertThat(result).hasSize(2);
        verify(enrollmentRepository).findEnrollmentsByStudentAndTerm(studentId, term);
        verifyNoInteractions(courseRepository);
    }

    @Test
    @DisplayName("Should Throw NoCoursesFoundException When No Enrollments")
    void shouldThrowNoCoursesFoundException() {
        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(studentId, term))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getEnrollmentsByStudent(studentId, term))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("No enrollments found for this student this term.");
    }

    @Test
    @DisplayName("Should Throw IllegalArgumentException When Student ID Is Null")
    void shouldThrowIllegalArgumentExceptionWhenStudentIdIsNull() {
        assertThatThrownBy(() -> service.getEnrollmentsByStudent(null, term))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student ID and Term cannot be null");

        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    @DisplayName("Should Throw IllegalArgumentException When Term Is Null")
    void shouldThrowIllegalArgumentExceptionWhenTermIsNull() {
        assertThatThrownBy(() -> service.getEnrollmentsByStudent(studentId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student ID and Term cannot be null");

        verifyNoInteractions(enrollmentRepository);
    }
}

@Tag("Functional")
@SpringBootTest
@Transactional
class EnrollmentQueryServiceFunctionalTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentQueryService service;

    private Student student;
    private Course math;
    private Course physics;
    private Term term;

    @BeforeEach
    void setup() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();

        student = studentRepository.save(TestUtils.createDefaultStudent());
        term = Term.current();

        math = TestUtils.createCourse("MAT", "Math", 4);
        math.setAvailableSeats(30);
        math.setSchedule(List.of(new ClassSchedule("Monday", "09:00", "11:00")));
        math = courseRepository.save(math);

        physics = TestUtils.createCourse("PHY", "Physics", 3);
        physics.setAvailableSeats(30);
        physics.setSchedule(List.of(new ClassSchedule("Tuesday", "09:00", "11:00")));
        physics = courseRepository.save(physics);

        enrollmentRepository.save(new Enrollment(student, math, term));
        enrollmentRepository.save(new Enrollment(student, physics, term));
    }

    @Test
    @DisplayName("Should Return All Enrollments for Student")
    void shouldReturnAllEnrollmentsForStudent() {
        List<Enrollment> result = service.getEnrollmentsByStudent(student.getId(), term);

        assertThat(result)
                .hasSize(2);
        assertThat(result.get(0).getCourse().getCode()).isEqualTo("MAT");
        assertThat(result.get(1).getCourse().getCode()).isEqualTo("PHY");
    }

    @Test
    @DisplayName("Should Throw NoCoursesFoundException for Student with No Enrollments")
    void shouldThrowNoCoursesFoundException() {
        Student student2 = studentRepository.save(TestUtils.createStudent("student2", "Student 2"));

        assertThatThrownBy(() -> service.getEnrollmentsByStudent(student2.getId(), term))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("No enrollments found for this student this term.");
    }
}