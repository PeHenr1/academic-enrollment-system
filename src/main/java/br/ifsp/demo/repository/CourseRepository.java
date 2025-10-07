package br.ifsp.demo.repository;

import br.ifsp.demo.domain.OfferedCourse;
import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    Optional<OfferedCourse> findByCode(String courseCode);

    List<OfferedCourse> findCourses();
}
