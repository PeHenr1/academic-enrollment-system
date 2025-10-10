// File: br.ifsp.demo.domain.Student.java

package br.ifsp.demo.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity // 1. Torna a classe persistível
@Table(name = "student")
public class Student {

    @Id // 2. Define 'id' como chave primária
    private String id;

    // Removendo 'final' para compatibilidade com o construtor vazio do Hibernate
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "student_completed_courses",
            joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "course_code")
    private List<String> completedCourses = new ArrayList<>();

    // 3. Construtor vazio necessário para Hibernate
    protected Student() {}

    public Student(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(List<String> completedCourses) {
        this.completedCourses = completedCourses;
    }

    public void addCompletedCourse(String courseCode) {
        this.completedCourses.add(courseCode);
    }
}