package br.ifsp.demo.controller;

import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.domain.Term;
import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.security.auth.AuthenticationInfoService;
import br.ifsp.demo.service.EnrollmentQueryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@AllArgsConstructor
public class EnrollmentQueryController {

    private final EnrollmentQueryService queryService;
    private final AuthenticationInfoService authService;

     @GetMapping("/my-courses")
    public ResponseEntity<?> getMyEnrollments() {
        try {
            String studentId = authService.getAuthenticatedStudentId();
            Term currentTerm = Term.current();

            List<Enrollment> enrollments = queryService.getEnrollmentsByStudent(studentId, currentTerm);


            return ResponseEntity.ok(enrollments);

        } catch (NoCoursesFoundException ex) {
            return ResponseEntity.ok(List.of());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Erro interno no servidor: " + ex.getMessage());
        }
    }
}