package br.ifsp.demo.controller;

import br.ifsp.demo.model.Course;
import br.ifsp.demo.service.CourseQueryService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/courses")
@AllArgsConstructor
public class CourseQueryController {

    private final CourseQueryService queryService;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        try {
            List<Course> courses = queryService.getCourses();
            return ResponseEntity.ok(courses);
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Course>> getCoursesByFilter(
            @Parameter
            @RequestParam(required = false) String name,

            @Parameter
            @RequestParam(required = false) String shift
    ) {
        try {
            List<Course> filtered = queryService.getCoursesByFilter(name, shift);
            return ResponseEntity.ok(filtered);
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
