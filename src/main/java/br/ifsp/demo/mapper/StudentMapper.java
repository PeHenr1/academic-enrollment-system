package br.ifsp.demo.mapper;

import br.ifsp.demo.domain.Student;
import br.ifsp.demo.infrastructure.persistence.StudentEntity;

import java.util.List;
import java.util.stream.Collectors;

public class StudentMapper {

    public static StudentEntity toEntity(Student student) {
        if (student == null) return null;
        StudentEntity entity = new StudentEntity();
        entity.setId(student.getId());
        entity.setName(student.getName());
        entity.setCompletedCourses(student.getCompletedCourses());
        return entity;
    }

    public static Student toDomain(StudentEntity entity) {
        if (entity == null) return null;
        Student student = new Student(entity.getId(), entity.getName());
        student.setCompletedCourses(entity.getCompletedCourses());
        return student;
    }

    public static List<StudentEntity> toEntityList(List<Student> students) {
        return students.stream().map(StudentMapper::toEntity).collect(Collectors.toList());
    }

    public static List<Student> toDomainList(List<StudentEntity> entities) {
        return entities.stream().map(StudentMapper::toDomain).collect(Collectors.toList());
    }
}
