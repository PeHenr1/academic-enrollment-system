package br.ifsp.demo.service;

import br.ifsp.demo.model.Course;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@Tag("TDD")
@Tag("UnitTest")
class EnrollmentValidationServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    private EnrollmentValidationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new EnrollmentValidationService(courseRepository, enrollmentRepository);
    }

    @Test
    @DisplayName("Should Reject Enrollment When Course Code Is Missing")
    void shouldRejectEnrollmentWhenCourseCodeIsMissing() {
        Course invalidCourse = new Course(null, "Programação I", "08:00-10:00", 4, List.of(), 40);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(invalidCourse));

        assertThatThrownBy(() -> service.enrollStudent(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course With Chosen Code Not Found");

        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Should Allow Enrollment When Course Is Valid")
    void shouldAllowEnrollmentWhenCourseIsValid() {
        Course validCourse = new Course("ADS101", "Programação I", "08:00-10:00", 4, List.of(), 40);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));

        service.enrollStudent(1L);

        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Should Reject Enrollment When No Seats Are Available")
    void shouldRejectEnrollmentWhenNoSeatsAreAvailable() {
        Course fullCourse = new Course("ENG301", "Cálculo III", "14:00-16:00", 4, List.of(), 0);
        when(courseRepository.findById(5L)).thenReturn(Optional.of(fullCourse));

        assertThatThrownBy(() -> service.enrollStudent(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No More Available Seats for This Courses");

        verify(courseRepository).findById(5L);
        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    @DisplayName("Should Reject Enrollment When Course ID Does Not Exist")
    void shouldRejectEnrollmentWhenCourseIdDoesNotExist() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enrollStudent(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course not found");

        verify(courseRepository).findById(999L);
        verifyNoInteractions(enrollmentRepository);
    }
}

@Tag("UnitTest")
@Tag("Functional")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnrollmentValidationServiceFunctionalTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentValidationService realService;

    @AfterAll
    void resetDatabase() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @BeforeEach
    void setupDatabase() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("Should Reject Enrollment When Course Code Is Missing")
    void shouldRejectEnrollmentWhenCourseCodeIsMissing() {
        Course invalidCourse = new Course(null, "Programação I", "08:00-10:00", 4, List.of(), 40);
        courseRepository.save(invalidCourse);

        assertThatThrownBy(() -> realService.enrollStudent(invalidCourse.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course With Chosen Code Not Found");

        assertThat(enrollmentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should Allow Enrollment When Course Is Valid")
    void shouldAllowEnrollmentWhenCourseIsValid() {
        Course validCourse = new Course("ADS101", "Programação I", "08:00-10:00", 4, List.of(), 40);
        courseRepository.save(validCourse);

        realService.enrollStudent(validCourse.getId());

        assertThat(enrollmentRepository.findAll()).hasSize(1);
        assertThat(enrollmentRepository.findAll().getFirst().isCanceled()).isFalse();
    }

    @Test
    @DisplayName("Should Reject Enrollment When No Seats Are Available")
    void shouldRejectEnrollmentWhenNoSeatsAreAvailable() {
        Course fullCourse = new Course("ENG301", "Cálculo III", "14:00-16:00", 4, List.of(), 0);
        courseRepository.save(fullCourse);

        assertThatThrownBy(() -> realService.enrollStudent(fullCourse.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No More Available Seats for This Courses");
    }

    @Test
    @DisplayName("Should Reject Enrollment When Course ID Does Not Exist")
    void shouldRejectEnrollmentWhenCourseIdDoesNotExist() {
        assertThatThrownBy(() -> realService.enrollStudent(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course not found");

        assertThat(enrollmentRepository.findAll()).isEmpty();
    }
}