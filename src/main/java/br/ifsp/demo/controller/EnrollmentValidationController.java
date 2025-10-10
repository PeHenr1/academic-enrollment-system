package br.ifsp.demo.controller;

import br.ifsp.demo.service.EnrollmentValidationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enrollments")
@AllArgsConstructor
public class EnrollmentValidationController {

    private final EnrollmentValidationService validationService;

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<String> enrollStudent(@PathVariable Long courseId) {
        try {
            validationService.enrollStudent(courseId);
            return ResponseEntity.ok("Student successfully enrolled in course " + courseId);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());

        } catch (IllegalStateException ex) {
            return ResponseEntity.status(400).body(ex.getMessage());

        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal server error: " + ex.getMessage());
        }
    }
}
