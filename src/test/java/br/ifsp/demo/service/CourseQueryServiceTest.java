package br.ifsp.demo.service;

import br.ifsp.demo.model.Course;
import br.ifsp.demo.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseQueryServiceTest {

    @Mock
    private CourseRepository repository;

    private CourseQueryService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CourseQueryService(repository);
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Return All Offered Courses For The Semester")
    void shouldReturnAllOfferedCoursesForTheSemester() {
        Course course1 = new Course("CS101", "Algorithms", "08:00-10:00", 4, List.of(), 40);
        Course course2 = new Course("CS102", "Databases", "10:00-12:00", 4, List.of("CS101"), 30);
        when(repository.findCourses()).thenReturn(List.of(course1, course2));

        List<Course> result = service.getCourses();

        assertEquals(2, result.size());
        assertEquals("CS101", result.getFirst().getCode());
        verify(repository).findCourses();
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Return Empty List When No Courses Offered")
    void shouldReturnEmptyListWhenNoCoursesOffered() {
        when(repository.findCourses()).thenReturn(List.of());

        List<Course> result = service.getCourses();

        assertTrue(result.isEmpty(), "Expected empty course list");
        verify(repository).findCourses();
    }
}