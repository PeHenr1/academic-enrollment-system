package br.ifsp.demo.domain;

import java.util.ArrayList;
import java.util.List;

public class Student {

    private final String id;
    private final String name;
    private List<String> completedCourses = new ArrayList<>();

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
