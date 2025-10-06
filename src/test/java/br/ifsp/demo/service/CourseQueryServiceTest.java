package br.ifsp.demo.service;

import br.ifsp.demo.model.Course;
import br.ifsp.demo.repository.CourseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseQueryServiceTest {

    @Mock
    private CourseRepository repository;

    private CourseQueryService service;

    static Stream<Arguments> courseFilterProvider() {

        Course course1 = new Course("ADS101", "Programação I", "08:00-10:00", 4, List.of(), 40);
        course1.setName("ADS");
        course1.setShift("Noturno");

        Course course2 = new Course("ADS201", "Engenharia de Software", "14:00-16:00", 4, List.of(), 35);
        course2.setName("ADS");
        course2.setShift("Noturno");

        return Stream.of(
                Arguments.of(List.of(course1)),
                Arguments.of(List.of(course1, course2)));
    }

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
    @ParameterizedTest
    @MethodSource("courseFilterProvider")
    void shouldReturnCoursesAccordingToFilters(List<Course> courses) {
        when(repository.findCourses()).thenReturn(courses);

        List<Course> filtered = service.getCoursesByFilter("ADS", "Noturno");

        assertTrue(filtered.stream().allMatch(c ->
                c.getName().equals("ADS") && c.getShift().equals("Noturno")));
        verify(repository).findCourses();
    }

    @Tag("TDD")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Return Empty List When No Course Matches Filter")
    void shouldReturnEmptyListWhenNoCourseMatchesFilter() {
        Course course1 = new Course("ADS101", "Programação I", "08:00-10:00", 4, List.of(), 40);
        course1.setName("ADS");
        course1.setShift("Noturno");

        when(repository.findCourses()).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter("BES", "Diurno");

        assertTrue(result.isEmpty(), "Expected empty list when no course matches filters");
    }
}

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CourseQueryServiceFunctionalTest {

    @Autowired
    private CourseRepository jpaRepository;

    @Autowired
    private CourseQueryService realService;

    @AfterAll
    void clearDatabase() {
        jpaRepository.deleteAll();
    }

    @BeforeEach
    void setupFunctional() {
        jpaRepository.deleteAll();
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Return All Offered Courses")
    void shouldReturnAllOfferedCourses() {

        Course course1 = new Course("ADS101", "Programação I", "08:00-10:00", 4, List.of(), 40);

        jpaRepository.save(course1);

        List<Course> result = realService.getCourses();

        assertEquals("ADS101", result.getFirst().getCode());
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Return Empty List When No Courses")
    void shouldReturnEmptyListWhenNoCourses() {
        List<Course> result = realService.getCourses();

        assertTrue(result.isEmpty(), "Empty List When There's No Courses On DB");
    }

    @Tag("Functional")
    @Tag("UnitTest")
    @Test
    @DisplayName("Should Filter Courses")
    void shouldFilterCourses() {

        Course course1 = new Course("ADS101", "Programação I", "08:00-10:00", 4, List.of(), 40);
        course1.setName("ADS");
        course1.setShift("Noturno");

        Course course2 = new Course("ADS201", "Engenharia de Software", "14:00-16:00", 4, List.of(), 35);
        course2.setName("ADS");
        course2.setShift("Noturno");

        jpaRepository.save(course1);
        jpaRepository.save(course2);

        List<Course> filtered = realService.getCoursesByFilter("ADS", "Noturno");

        assertTrue(filtered.stream().allMatch(c ->
                c.getName().equals("ADS") && c.getShift().equals("Noturno")));
    }
}