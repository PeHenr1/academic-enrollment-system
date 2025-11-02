package br.ifsp.demo.service;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.repository.CourseRepository;
import br.ifsp.demo.util.TestUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("TDD")
@Tag("UnitTest")
class CourseQueryServiceTest {

    @Mock
    private CourseRepository repository;

    private CourseQueryService service;
    private Course course1;
    private Course course2;

    static Stream<Arguments> courseFilterProvider() {
        var course1 = TestUtils.createCourse("ADS101", "Programação I", 4);
        course1.setName("ADS");
        course1.setShift("Noturno");

        var course2 = TestUtils.createCourse("ADS201", "Engenharia de Software", 4);
        course2.setName("ADS");
        course2.setShift("Noturno");

        return Stream.of(
                Arguments.of(List.of(course1)),
                Arguments.of(List.of(course1, course2))
        );
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CourseQueryService(repository);

        course1 = TestUtils.createCourse("ADS101", "Programação I", 4);
        course1.setName("ADS");
        course1.setShift("Noturno");

        course2 = TestUtils.createCourse("ADS201", "Engenharia de Software", 4);
        course2.setName("ADS");
        course2.setShift("Noturno");
    }

    @Test
    @DisplayName("Should Return All Offered Courses For The Semester")
    void shouldReturnAllOfferedCoursesForTheSemester() {
        when(repository.findCourses()).thenReturn(List.of(course1, course2));

        List<Course> result = service.getCourses();

        assertEquals(2, result.size());
        assertEquals("ADS101", result.get(0).getCode());
        verify(repository).findCourses();
    }

    @Test
    @DisplayName("Should Return Empty List When No Courses Offered")
    void shouldReturnEmptyListWhenNoCoursesOffered() {
        when(repository.findCourses()).thenReturn(List.of());

        List<Course> result = service.getCourses();

        assertTrue(result.isEmpty(), "Expected empty course list");
        verify(repository).findCourses();
    }

    @Test
    @DisplayName("Should Throw Exception When Repository Returns Null")
    void shouldThrowExceptionWhenRepositoryReturnsNull() {
        when(repository.findCourses()).thenReturn(null);

        assertThatThrownBy(() -> service.getCourses())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to load offered courses");
        verify(repository).findCourses();
    }

    @ParameterizedTest
    @MethodSource("courseFilterProvider")
    void shouldReturnCoursesAccordingToFilters(List<Course> courses) {
        when(repository.findCourses()).thenReturn(courses);

        List<Course> filtered = service.getCoursesByFilter("ADS", "Noturno");

        assertTrue(filtered.stream().allMatch(c ->
                c.getName().equals("ADS") && c.getShift().equals("Noturno")));
        verify(repository).findCourses();
    }

    @Test
    @DisplayName("Should Return Empty List When No Course Matches Filter")
    void shouldReturnEmptyListWhenNoCourseMatchesFilter() {
        when(repository.findCourses()).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter("BES", "Diurno");

        assertTrue(result.isEmpty(), "Expected empty list when no course matches filters");
    }
}

@SpringBootTest
@Tag("Functional")
@Tag("UnitTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CourseQueryServiceFunctionalTest {

    @Autowired
    private CourseRepository jpaRepository;

    @Autowired
    private CourseQueryService realService;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setupFunctional() {
        jpaRepository.deleteAll();

        course1 = TestUtils.createCourse("ADS101", "Programação I", 4);
        course1.setName("ADS");
        course1.setShift("Noturno");

        course2 = TestUtils.createCourse("ADS201", "Engenharia de Software", 4);
        course2.setName("ADS");
        course2.setShift("Noturno");
    }

    @AfterAll
    void clearDatabase() {
        jpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should Return All Offered Courses")
    void shouldReturnAllOfferedCourses() {
        jpaRepository.save(course1);

        List<Course> result = realService.getCourses();

        assertEquals("ADS101", result.get(0).getCode());
    }

    @Test
    @DisplayName("Should Return Empty List When No Courses")
    void shouldReturnEmptyListWhenNoCourses() {
        List<Course> result = realService.getCourses();
        assertTrue(result.isEmpty(), "Empty List When There's No Courses On DB");
    }

    @Test
    @DisplayName("Should Filter Courses")
    void shouldFilterCourses() {
        jpaRepository.save(course1);
        jpaRepository.save(course2);

        List<Course> filtered = realService.getCoursesByFilter("ADS", "Noturno");

        assertTrue(filtered.stream().allMatch(c ->
                c.getName().equals("ADS") && c.getShift().equals("Noturno")));
    }

    @Test
    @DisplayName("Should Return Empty List When No Course Matches Filters")
    void shouldReturnEmptyListWhenNoCourseMatchesFilters() {
        jpaRepository.save(course1);

        List<Course> filtered = realService.getCoursesByFilter("ENG", "Diurno");

        assertTrue(filtered.isEmpty(), "Expected empty list when no course matches filters");
    }
}

@Tag("Structural")
@Tag("UnitTest")
class CourseQueryServiceStructuralTest {

    @Mock
    private CourseRepository repository;

    private CourseQueryService service;

    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CourseQueryService(repository);

        course1 = TestUtils.createCourse("ADS101", "Programação I", 4);
        course1.setName("ADS");
        course1.setShift("Noturno");

        course2 = TestUtils.createCourse("BES101", "Banco de Dados", 4);
        course2.setName("BES");
        course2.setShift("Diurno");

        course3 = TestUtils.createCourse("ADS201", "Engenharia de Software", 4);
        course3.setName("ADS");
        course3.setShift("Diurno");
    }

    @Test
    @DisplayName("Should filter only by course name when shift is null")
    void shouldFilterOnlyByNameWhenShiftIsNull() {
        when(repository.findCourses()).thenReturn(List.of(course1, course2, course3));

        var result = service.getCoursesByFilter("ADS", null);

        assertTrue(result.stream().allMatch(c -> c.getName().equals("ADS")));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should filter only by shift when course name is null")
    void shouldFilterOnlyByShiftWhenNameIsNull() {
        when(repository.findCourses()).thenReturn(List.of(course1, course2, course3));

        var result = service.getCoursesByFilter(null, "Diurno");

        assertTrue(result.stream().allMatch(c -> c.getShift().equals("Diurno")));
        assertEquals(2, result.size());
    }
}