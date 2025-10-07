package br.ifsp.demo.repository;

import br.ifsp.demo.domain.OfferedCourse;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.domain.Student;
import br.ifsp.demo.model.Course;

import java.util.List;

public interface EnrollmentRepository {

    List<OfferedCourse> findEnrollmentsByStudentAndTerm(String studentId, Term term);

    int calculateTotalCredits(String studentId, Term term);

    void saveEnrollment(Student student, OfferedCourse course);
}
