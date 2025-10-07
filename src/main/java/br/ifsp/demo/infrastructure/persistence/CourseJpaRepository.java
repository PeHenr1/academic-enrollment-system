package br.ifsp.demo.infrastructure.persistence;

import br.ifsp.demo.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseJpaRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String courseCode);
}
