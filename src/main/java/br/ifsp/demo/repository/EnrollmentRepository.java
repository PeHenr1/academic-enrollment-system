// File: br.ifsp.demo.repository.EnrollmentRepository.java

package br.ifsp.demo.repository;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.term.year = :#{#term.year} AND e.term.semester = :#{#term.semester}")
    List<Enrollment> findEnrollmentsByStudentAndTerm(String studentId, Term term);

    default void saveEnrollment(Student student, Course course, Term term) {
        save(new Enrollment(student, course, term));
    }

    @Query("SELECT SUM(e.course.credits) FROM Enrollment e WHERE e.student.id = :studentId AND e.term.year = :#{#term.year} AND e.term.semester = :#{#term.semester}")
    Integer calculateTotalCredits(String studentId, Term term);
}
