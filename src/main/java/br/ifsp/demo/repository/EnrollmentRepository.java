package br.ifsp.demo.repository;

import br.ifsp.demo.infrastructure.persistence.EnrollmentEntity;
import br.ifsp.demo.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    @Query("""
        SELECT e FROM EnrollmentEntity e
        WHERE e.student.id = :studentId
          AND e.term.year = :#{#term.year}
          AND e.term.semester = :#{#term.semester}""")
    List<EnrollmentEntity> findEnrollmentsByStudentAndTerm(String studentId, Term term);

    @Query("""
        SELECT SUM(e.course.credits) FROM EnrollmentEntity e
        WHERE e.student.id = :studentId
          AND e.term.year = :#{#term.year}
          AND e.term.semester = :#{#term.semester}""")
    Integer calculateTotalCredits(String studentId, Term term);
}
