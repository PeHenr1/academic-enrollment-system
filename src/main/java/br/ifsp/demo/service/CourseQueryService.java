package br.ifsp.demo.service;

import br.ifsp.demo.model.Course;
import br.ifsp.demo.repository.CourseRepository;

import java.util.List;

public class CourseQueryService {

    private final CourseRepository repository;

    public CourseQueryService(CourseRepository repository) {
        this.repository = repository;
    }

    public List<Course> getCourses() {
        List<Course> courses = repository.findCourses();

        return courses.stream().toList();
    }
}
