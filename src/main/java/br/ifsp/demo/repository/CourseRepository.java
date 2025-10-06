package br.ifsp.demo.repository;

import br.ifsp.demo.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    default List<Course> findCourses() {
        return findAll();
    }
}
