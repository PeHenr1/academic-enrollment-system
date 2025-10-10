package br.ifsp.demo.controller;

import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.NoCoursesFoundException;
import br.ifsp.demo.domain.Course;
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

    @GetMapping("/{id}/courses")
    public ResponseEntity<?> getCoursesByEnrollment(@PathVariable("id") Long enrollmentId) {
        try {
            List<Course> courses = queryService.getCoursesByEnrollment(enrollmentId);
            return ResponseEntity.ok(courses);

        } catch (EnrollmentNotFoundException ex) {
            return ResponseEntity.status(400).body(ex.getMessage());

        } catch (NoCoursesFoundException ex) {
            return ResponseEntity.status(200).body(ex.getMessage());

        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Erro interno no servidor: " + ex.getMessage());
        }
    }
}
