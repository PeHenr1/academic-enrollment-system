package br.ifsp.demo.repository;

import br.ifsp.demo.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByEnrollmentId(Long enrollmentId);
    default List<Course> findCourses() {
        return findAll();
    }
    Optional<Course> findByCode(String code);
}