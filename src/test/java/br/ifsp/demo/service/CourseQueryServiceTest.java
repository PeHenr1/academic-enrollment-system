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

import static org.assertj.core.api.Assertions.*;
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

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Throw Exception When Repository Returns Null")
    void shouldThrowExceptionWhenRepositoryReturnsNull() {
        when(repository.findCourses()).thenReturn(null);

        assertThatThrownBy(() -> service.getCourses())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to load offered courses");
        verify(repository).findCourses();
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Return Courses That Match The Filters")
    void shouldReturnCoursesThatMatchTheFilters() {
        Course course1 = new Course("ADS101", "Programação I", "08:00-10:00", 4, List.of(), 40);
        course1.setName("ADS");
        course1.setShift("Noturno");

        Course course2 = new Course("ADS201", "Engenharia De Software", "14:00-16:00", 4, List.of(), 35);
        course2.setName("ADS");
        course2.setShift("Diurno");

        when(repository.findCourses()).thenReturn(List.of(course1, course2));
        List<Course> filtered = service.getCoursesByFilter("ADS", null);

        assertEquals(2, filtered.size(), "Should return only courses matching the filters");
        assertTrue(filtered.stream().allMatch(c -> c.getName().equals("ADS")));
        verify(repository).findCourses();
    }
}