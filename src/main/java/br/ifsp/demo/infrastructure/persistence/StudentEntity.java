package br.ifsp.demo.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student")
public class StudentEntity {

    @Id
    private String id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "student_completed_courses", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "course_code")
    private List<String> completedCourses = new ArrayList<>();

    public StudentEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getCompletedCourses() { return completedCourses; }
    public void setCompletedCourses(List<String> completedCourses) { this.completedCourses = completedCourses; }
}
