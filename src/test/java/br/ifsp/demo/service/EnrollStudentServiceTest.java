package br.ifsp.demo.service;

import br.ifsp.demo.domain.ClassSchedule;
import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.repository.EnrollmentRepository;
import br.ifsp.demo.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    private Course course;
    private Term term;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        student = new Student("123", "John Doe");
        term = new Term(2025, 1); // Term fixo para testes
        course = new Course("IFSP101", "Software Engineering", 4);
        course.setAvailableSeats(5);
    }

    @Test
    void shouldEnrollStudentSuccessfullyWhenAllRulesAreSatisfied() {
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(course));
        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term))
                .thenReturn(new ArrayList<>());

        enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term);

        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void shouldThrowExceptionWhenPrerequisiteNotCompleted() {
        course.setPrerequisites(new ArrayList<>(List.of("IFSP201")));
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    void shouldThrowExceptionWhenCourseAlreadyCompleted() {
        student.setCompletedCourses(new ArrayList<>(List.of("IFSP101")));
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Course already completed");
    }

    @Test
    void shouldThrowExceptionWhenExceedingMaxCredits() {
        course.setCredits(10);
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(course));

        Course existingCourse = new Course("IFSP102", "Algorithms", 15);
        Enrollment existingEnrollment = new Enrollment(student, existingCourse, term);

        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term))
                .thenReturn(new ArrayList<>(List.of(existingEnrollment)));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximum of 20 credits exceeded");
    }

    @Test
    void shouldThrowExceptionWhenScheduleConflicts() {
        ClassSchedule s1 = new ClassSchedule("Monday", "10:00", "12:00");
        course.setSchedule(new ArrayList<>(List.of(s1)));

        Course enrolledCourse = new Course("IFSP102", "Algorithms", 4);
        enrolledCourse.setSchedule(new ArrayList<>(List.of(new ClassSchedule("Monday", "11:00", "13:00"))));
        Enrollment existingEnrollment = new Enrollment(student, enrolledCourse, term);

        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(course));
        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term))
                .thenReturn(new ArrayList<>(List.of(existingEnrollment)));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected");
    }

    @Test
    void shouldThrowExceptionWhenNoSeatsAvailable() {
        course.setAvailableSeats(0);
        when(courseRepository.findByCode("IFSP101")).thenReturn(Optional.of(course));
        when(enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term))
                .thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }
}

@SpringBootTest
@Transactional
@Tag("Functional")
class EnrollStudentServiceFunctionalTest {

    @Autowired
    private EnrollStudentService enrollStudentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Student student;
    private Term term;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new Student("123", "John Doe");
        term = Term.current();

        // Salva o Student antes de criar qualquer matrícula
        studentRepository.save(student);

        course = new Course("IFSP101", "Software Engineering", 4);
        course.setAvailableSeats(5);
        courseRepository.save(course);
    }

    @Test
    void shouldEnrollStudentSuccessfully() {
        enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term);

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentAndTerm(student.getId(), term);

        assertThat(enrollments).hasSize(1);
        assertThat(enrollments.get(0).getCourse().getCode()).isEqualTo("IFSP101");
    }

    @Test
    void shouldThrowExceptionWhenNoSeatsAvailable() {
        course.setAvailableSeats(0);
        courseRepository.save(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No seats available");
    }

    @Test
    void shouldThrowExceptionWhenPrerequisiteNotCompleted() {
        course.setPrerequisites(new ArrayList<>(List.of("IFSP201")));
        courseRepository.save(course);

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Missing prerequisite");
    }

    @Test
    void shouldThrowExceptionWhenScheduleConflicts() {
        Course c1 = new Course("IFSP102", "Algorithms", 4);
        c1.setSchedule(new ArrayList<>(List.of(new ClassSchedule("Monday", "11:00", "13:00"))));
        course.setSchedule(new ArrayList<>(List.of(new ClassSchedule("Monday", "10:00", "12:00"))));

        courseRepository.saveAll(new ArrayList<>(List.of(c1, course)));
        enrollmentRepository.save(new Enrollment(student, c1, term));

        assertThatThrownBy(() -> enrollStudentService.enroll(student, new ArrayList<>(List.of("IFSP101")), term))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Schedule conflict detected");
    }
}
