package br.ifsp.demo.repository;

import br.ifsp.demo.infrastructure.persistence.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    Optional<CourseEntity> findByCode(String code);
    List<CourseEntity> findByEnrollmentId(Long enrollmentId);
    default List<CourseEntity> findCourses() {
        return findAll();
    }
}