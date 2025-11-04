package br.ifsp.demo.service;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseQueryService {

    private final CourseRepository repository;

    public CourseQueryService(CourseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = false)
    public List<Course> getCourses() {
        List<Course> courses = repository.findCoursesByFilter(null, null);

        if (courses == null) {
            throw new IllegalStateException("Failed to load offered courses");
        }
        return courses;
    }

    @Transactional(readOnly = false)
    public List<Course> getCoursesByFilter(String courseName, String shift) {
        String nameFilter = (courseName == null || courseName.isBlank()) ? null : courseName;
        String shiftFilter = (shift == null || shift.isBlank()) ? null : shift;

        return repository.findCoursesByFilter(nameFilter, shiftFilter);
    }
}