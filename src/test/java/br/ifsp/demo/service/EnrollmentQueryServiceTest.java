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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class EnrollmentQueryServiceTest {

    private EnrollmentRepository enrollmentRepository;
    private CourseRepository courseRepository;
    private EnrollmentQueryService service;

    private final Long enrollmentId = 1L;
    private Course math;
    private Course physics;

    @BeforeEach
    void setup() {
        enrollmentRepository = mock(EnrollmentRepository.class);
        courseRepository = mock(CourseRepository.class);
        service = new EnrollmentQueryService(enrollmentRepository, courseRepository);

        math = TestUtils.createCourse("MAT", "Math", 4);
        math.setSchedule(List.of(new ClassSchedule("Monday", "08:00", "10:00")));
        math.setAvailableSeats(30);

        physics = TestUtils.createCourse("PHY", "Physics", 3);
        physics.setSchedule(List.of(new ClassSchedule("Monday", "10:00", "12:00")));
        physics.setAvailableSeats(40);
    }

    @Test
    @DisplayName("Should Return Courses for Enrollment")
    void shouldReturnCoursesForEnrollment() {
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(true);
        when(courseRepository.findByEnrollmentId(enrollmentId)).thenReturn(List.of(math, physics));

        List<Course> result = service.getCoursesByEnrollment(enrollmentId);

        assertThat(result).hasSize(2)
                .extracting(Course::getName)
                .containsExactlyInAnyOrder("Math", "Physics");

        verify(enrollmentRepository).existsById(enrollmentId);
        verify(courseRepository).findByEnrollmentId(enrollmentId);
    }

    @Test
    @DisplayName("Should Throw NoCoursesFoundException When No Courses")
    void shouldThrowNoCoursesFoundException() {
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(true);
        when(courseRepository.findByEnrollmentId(enrollmentId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getCoursesByEnrollment(enrollmentId))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("No courses found for this enrollment");
    }

    @Test
    @DisplayName("Should Throw EnrollmentNotFoundException When Enrollment Does Not Exist")
    void shouldThrowEnrollmentNotFoundException() {
        Long invalidId = 999L;
        when(enrollmentRepository.existsById(invalidId)).thenReturn(false);

        assertThatThrownBy(() -> service.getCoursesByEnrollment(invalidId))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Enrollment not found or inactive");
    }

    @Test
    @DisplayName("Should Reject When Enrollment ID Is Null")
    void shouldRejectWhenEnrollmentIdIsNull() {
        assertThatThrownBy(() -> service.getCoursesByEnrollment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null");

        verifyNoInteractions(enrollmentRepository);
        verifyNoInteractions(courseRepository);
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
    private Enrollment enrollment;
    private Course initialCourse;

    private Course math;
    private Course physics;

    @BeforeEach
    void setup() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();

        student = studentRepository.save(TestUtils.createDefaultStudent());

        initialCourse = TestUtils.createCourse("INIT", "Initial Course", 1);
        initialCourse.setAvailableSeats(1);
        initialCourse.setSchedule(List.of(new ClassSchedule("Monday", "08:00", "09:00")));
        initialCourse = courseRepository.save(initialCourse);

        enrollment = enrollmentRepository.save(new Enrollment(student, initialCourse, Term.current()));

        math = TestUtils.createCourse("MAT", "Math", 4);
        math.setSchedule(List.of(new ClassSchedule("Monday", "09:00", "11:00")));
        math.setAvailableSeats(30);
        math.setEnrollment(enrollment);

        physics = TestUtils.createCourse("PHY", "Physics", 3);
        physics.setSchedule(List.of(new ClassSchedule("Monday", "11:00", "13:00")));
        physics.setAvailableSeats(40);
        physics.setEnrollment(enrollment);
    }

    @Test
    @DisplayName("Should Return Multiple Courses for Enrollment")
    void shouldReturnMultipleCourses() {
        courseRepository.saveAll(List.of(math, physics));

        List<Course> result = service.getCoursesByEnrollment(enrollment.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(Course::getName)
                .containsExactlyInAnyOrder("Math", "Physics");
    }

    @Test
    @DisplayName("Should Throw NoCoursesFoundException When Enrollment Has No Courses")
    void shouldThrowNoCoursesFoundException() {
        Student noCoursesStudent = studentRepository.save(TestUtils.createStudent("NOCOURSES", "Student Without Courses"));

        Course tempCourse = TestUtils.createCourse("TEMP", "Temporary Course", 1);
        tempCourse.setAvailableSeats(1);
        tempCourse = courseRepository.save(tempCourse);

        Enrollment enrollmentWithNoCourses = enrollmentRepository.save(new Enrollment(noCoursesStudent, tempCourse, Term.current()));
        tempCourse.setEnrollment(null);
        courseRepository.save(tempCourse);

        List<Course> coursesForEnrollment = courseRepository.findByEnrollmentId(enrollmentWithNoCourses.getId());
        assertThat(coursesForEnrollment).isEmpty();

        assertThatThrownBy(() -> service.getCoursesByEnrollment(enrollmentWithNoCourses.getId()))
                .isInstanceOf(NoCoursesFoundException.class)
                .hasMessage("No courses found for this enrollment");
    }

    @Test
    @DisplayName("Should Throw EnrollmentNotFoundException When Enrollment Does Not Exist")
    void shouldThrowEnrollmentNotFoundException() {
        Long invalidId = 999L;
        assertThatThrownBy(() -> service.getCoursesByEnrollment(invalidId))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("Enrollment not found or inactive");
    }

    @Test
    @DisplayName("Should Reject When Enrollment Is Null or Not Found")
    void shouldRejectWhenEnrollmentIsNullOrNotFound() {
        assertThatThrownBy(() -> service.getCoursesByEnrollment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null");
    }
}
