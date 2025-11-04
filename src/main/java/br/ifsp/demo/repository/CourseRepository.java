package br.ifsp.demo.repository;

import br.ifsp.demo.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByEnrollmentId(Long enrollmentId);
    Optional<Course> findByCode(String code);

    @Query("SELECT c FROM Course c WHERE " +
            "(:name IS NULL OR lower(c.name) LIKE lower(concat('%', :name, '%'))) AND " +
            "(:shift IS NULL OR lower(c.shift) LIKE lower(concat('%', :shift, '%')))")
    List<Course> findCoursesByFilter(
            @Param("name") String name,
            @Param("shift") String shift
    );
    default List<Course> findCourses() {
        return findAll();
    }
}