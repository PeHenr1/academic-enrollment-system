package br.ifsp.demo.repository;

import br.ifsp.demo.model.Course;

import java.util.List;

public interface CourseRepository {
    List<Course> findCourses();
}
