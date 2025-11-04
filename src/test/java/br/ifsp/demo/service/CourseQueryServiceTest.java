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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(repository.findCoursesByFilter(null, null)).thenReturn(List.of(course1, course2));

        List<Course> result = service.getCourses();

        assertEquals(2, result.size());
        assertEquals("ADS101", result.get(0).getCode());
        verify(repository).findCoursesByFilter(null, null);
    }

    @Test
    @DisplayName("Should Return Empty List When No Courses Offered")
    void shouldReturnEmptyListWhenNoCoursesOffered() {
        when(repository.findCoursesByFilter(null, null)).thenReturn(List.of());

        List<Course> result = service.getCourses();

        assertTrue(result.isEmpty(), "Expected empty course list");
        verify(repository).findCoursesByFilter(null, null);
    }

    @Test
    @DisplayName("Should Throw Exception When Repository Returns Null")
    void shouldThrowExceptionWhenRepositoryReturnsNull() {
        when(repository.findCoursesByFilter(null, null)).thenReturn(null);

        assertThatThrownBy(() -> service.getCourses())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to load offered courses");
        verify(repository).findCoursesByFilter(null, null);
    }

    @ParameterizedTest
    @MethodSource("courseFilterProvider")
    @DisplayName("Should Return Courses According To Filters")
    void shouldReturnCoursesAccordingToFilters(List<Course> courses) {
        when(repository.findCoursesByFilter("ADS", "Noturno")).thenReturn(courses);

        List<Course> filtered = service.getCoursesByFilter("ADS", "Noturno");

        assertTrue(filtered.stream().allMatch(c ->
                c.getName().equals("ADS") && c.getShift().equals("Noturno")));

        verify(repository).findCoursesByFilter("ADS", "Noturno");
    }

    @Test
    @DisplayName("Should Return Empty List When No Course Matches Filter")
    void shouldReturnEmptyListWhenNoCourseMatchesFilter() {
        when(repository.findCoursesByFilter("BES", "Diurno")).thenReturn(List.of());

        List<Course> result = service.getCoursesByFilter("BES", "Diurno");

        assertTrue(result.isEmpty(), "Expected empty list when no course matches filters");
        verify(repository).findCoursesByFilter("BES", "Diurno");
    }

    @Test
    @DisplayName("Should Treat Empty String Filter as Null")
    void shouldTreatEmptyStringFilterAsNull() {
        when(repository.findCoursesByFilter(null, null)).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter("", "");

        verify(repository).findCoursesByFilter(null, null);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should Treat Blank String Filter as Null")
    void shouldTreatBlankStringFilterAsNull() {
        when(repository.findCoursesByFilter(null, null)).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter("   ", " ");

        verify(repository).findCoursesByFilter(null, null);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should Treat Blank Name Filter as Null when Shift is Valid")
    void shouldTreatBlankNameFilterAsNullWhenShiftIsValid() {
        when(repository.findCoursesByFilter(null, "Noturno")).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter(" ", "Noturno");

        verify(repository).findCoursesByFilter(null, "Noturno");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should Treat Blank Shift Filter as Null when Name is Valid")
    void shouldTreatBlankShiftFilterAsNullWhenNameIsValid() {
        when(repository.findCoursesByFilter("ADS", null)).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter("ADS", " ");

        verify(repository).findCoursesByFilter("ADS", null);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should Handle Null Name With Valid Shift")
    void shouldHandleNullNameWithValidShift() {
        when(repository.findCoursesByFilter(null, "Noturno")).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter(null, "Noturno");

        verify(repository).findCoursesByFilter(null, "Noturno");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should Handle Valid Name With Null Shift")
    void shouldHandleValidNameWithNullShift() {
        when(repository.findCoursesByFilter("ADS", null)).thenReturn(List.of(course1));

        List<Course> result = service.getCoursesByFilter("ADS", null);

        verify(repository).findCoursesByFilter("ADS", null);
        assertThat(result).hasSize(1);
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
    @Transactional(readOnly = false)
    void shouldReturnAllOfferedCourses() {
        jpaRepository.save(course1);

        List<Course> result = realService.getCourses();

        assertEquals("ADS101", result.get(0).getCode());
    }

    @Test
    @DisplayName("Should Return Empty List When No Courses")
    @Transactional(readOnly = false)
    void shouldReturnEmptyListWhenNoCourses() {
        List<Course> result = realService.getCourses();
        assertTrue(result.isEmpty(), "Empty List When There's No Courses On DB");
    }

    @Test
    @DisplayName("Should Filter Courses")
    @Transactional(readOnly = false)
    void shouldFilterCourses() {
        jpaRepository.save(course1);
        jpaRepository.save(course2);

        List<Course> filtered = realService.getCoursesByFilter("ADS", "Noturno");

        assertTrue(filtered.stream().allMatch(c ->
                c.getName().equals("ADS") && c.getShift().equals("Noturno")));
    }

    @Test
    @DisplayName("Should Return Empty List When No Course Matches Filters")
    @Transactional(readOnly = false)
    void shouldReturnEmptyListWhenNoCourseMatchesFilters() {
        jpaRepository.save(course1);

        List<Course> filtered = realService.getCoursesByFilter("ENG", "Diurno");

        assertTrue(filtered.isEmpty(), "Expected empty list when no course matches filters");
    }

    @Test
    @DisplayName("Should Return All Courses When Filters Are Blank")
    @Transactional(readOnly = false)
    void shouldReturnAllCoursesWhenFiltersAreBlank() {
        jpaRepository.save(course1);
        jpaRepository.save(course2);

        List<Course> filtered = realService.getCoursesByFilter(" ", " ");

        assertThat(filtered).hasSize(2);
    }

    @Test
    @DisplayName("Should Return All Courses When Filters Are Empty")
    @Transactional(readOnly = false)
    void shouldReturnAllCoursesWhenFiltersAreEmpty() {
        jpaRepository.save(course1);
        jpaRepository.save(course2);

        List<Course> filtered = realService.getCoursesByFilter("", "");

        assertThat(filtered).hasSize(2);
    }
}