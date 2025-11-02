package br.ifsp.demo.util;

import br.ifsp.demo.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TestUtils {

    private TestUtils() {}

    public static Student createDefaultStudent() {
        return new Student("123", "John Doe");
    }

    public static Student createStudent(String ra, String name) {
        return new Student(ra, name);
    }

    public static Student createStudentWithCompletedCourses(String ra, String name, List<String> completedCourses) {
        Student student = createStudent(ra, name);
        student.setCompletedCourses(new ArrayList<>(completedCourses));
        return student;
    }

    public static Course createDefaultCourse() {
        return new Course("IFSP101", "Software Engineering", 4);
    }

    public static Course createCourse(String code, String name, int credits) {
        return new Course(code, name, credits);
    }

    public static Course createCourseWithPrerequisites(String code, String name, int credits, List<String> prerequisites) {
        Course course = createCourse(code, name, credits);
        course.setPrerequisites(new ArrayList<>(prerequisites));
        return course;
    }

    public static Course createCourseWithSchedule(String code, String name, int credits, List<ClassSchedule> schedule) {
        Course course = createCourse(code, name, credits);
        course.setSchedule(new ArrayList<>(schedule));
        return course;
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

    public static Enrollment createEnrollment(Student student, Course course, Term term) {
        return  new Enrollment(student, course, term);
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

    public static ClassSchedule createClassSchedule(String day, String startTime, String endTime) {
        return new ClassSchedule(day, startTime, endTime);
    }
}
