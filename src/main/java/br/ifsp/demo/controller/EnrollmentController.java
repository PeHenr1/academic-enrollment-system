package br.ifsp.demo.controller;

import br.ifsp.demo.domain.Student;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.exception.BusinessRuleException;
import br.ifsp.demo.repository.StudentRepository;
import br.ifsp.demo.service.EnrollStudentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@AllArgsConstructor
public class EnrollmentController {

    private final EnrollStudentService enrollStudentService;
    private final StudentRepository studentRepository;

    @PostMapping("/enroll/{studentId}")
    public ResponseEntity<String> enrollStudent(
            @PathVariable String studentId,
            @RequestBody List<String> courseCodes) {

        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new BusinessRuleException("Student not found"));

            Term term = Term.current();

            enrollStudentService.enroll(student, courseCodes, term);

            return ResponseEntity.ok("Student successfully enrolled in " + courseCodes.size() + " course(s).");

        } catch (BusinessRuleException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error: " + ex.getMessage());
        }
    }
}