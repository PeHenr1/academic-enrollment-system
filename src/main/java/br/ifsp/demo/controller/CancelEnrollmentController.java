package br.ifsp.demo.controller;

import br.ifsp.demo.security.auth.AuthenticationInfoService;
import br.ifsp.demo.service.CancelEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/enrollments")
@AllArgsConstructor
public class CancelEnrollmentController {

    private final CancelEnrollmentService cancelService;
    private final AuthenticationInfoService authService;

    @Operation(summary = "Cancel an enrollment by ID")
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<String> cancelEnrollment(@PathVariable Long id) {
        try {
           String studentId = authService.getAuthenticatedStudentId();

            boolean canceled = cancelService.cancelEnrollment(id, studentId);

            if (!canceled) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok("Enrollment successfully canceled.");

        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {     return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}