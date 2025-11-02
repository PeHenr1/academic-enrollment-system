package br.ifsp.demo.util;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public final class TestUtils {

    private TestUtils() {}

    public static Student createDefaultStudent() {
        return new Student("123", "John Doe");
    }

    public static Student createStudent(String ra, String name) {
        return new Student(ra, name);
    }

    public static Course createDefaultCourse() {
        return new Course("IFSP101", "Software Engineering", 4);
    }

    public static Course createCourse(String code, String name, int credits) {
        return new Course(code, name, credits);
    }

    public static Term createDefaultTerm() {
        return new Term(2025, 1);
    }

    public static Term createTerm(int year, int semester) {
        return new Term(year, semester);
    }

    public static Enrollment createDefaultEnrollment() {
        return new Enrollment(createDefaultStudent(), createDefaultCourse(), createDefaultTerm());
    }

    public static Enrollment createEnrollmentWithDeadline(LocalDate deadline) {
        Enrollment enrollment = createDefaultEnrollment();
        enrollment.setDeadline(deadline);
        return enrollment;
    }

    public static Enrollment createEnrollmentWithId(Long id) {
        Enrollment enrollment = createDefaultEnrollment();
        ReflectionTestUtils.setField(enrollment, "id", id);
        return enrollment;
    }

    public static Enrollment createCanceledEnrollment() {
        Enrollment enrollment = createEnrollmentWithDeadline(LocalDate.now().plusDays(1));
        enrollment.cancel();
        return enrollment;
    }

    public static Enrollment createExpiredEnrollment() {
        Enrollment enrollment = createDefaultEnrollment();
        enrollment.setDeadline(LocalDate.now().minusDays(1));
        return enrollment;
    }
}
