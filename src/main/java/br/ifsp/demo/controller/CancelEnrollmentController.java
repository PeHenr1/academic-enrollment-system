package br.ifsp.demo.controller;

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

    @Operation(summary = "Cancel an enrollment by ID")
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<String> cancelEnrollment(@PathVariable Long id) {
        try {
            boolean canceled = cancelService.cancelEnrollment(id);

            if (!canceled) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok("Enrollment successfully canceled.");

        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}
