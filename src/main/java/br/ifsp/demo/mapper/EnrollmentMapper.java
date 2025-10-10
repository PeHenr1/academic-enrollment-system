package br.ifsp.demo.mapper;

import br.ifsp.demo.domain.Enrollment;
import br.ifsp.demo.infrastructure.persistence.EnrollmentEntity;

import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentMapper {

    public static EnrollmentEntity toEntity(Enrollment enrollment) {
        if (enrollment == null) return null;
        EnrollmentEntity entity = new EnrollmentEntity();
        entity.setId(enrollment.getId());
        entity.setStudent(enrollment.getStudent());
        entity.setCourse(enrollment.getCourse());
        entity.setTerm(enrollment.getTerm());
        entity.setCancellationDeadline(enrollment.getCancellationDeadline());
        entity.setCanceled(enrollment.isCanceled());
        entity.setCourses(enrollment.getCourses());
        return entity;
    }

    public static Enrollment toDomain(EnrollmentEntity entity) {
        if (entity == null) return null;
        Enrollment enrollment = new Enrollment();
        enrollment.setId(entity.getId());
        enrollment.setStudent(entity.getStudent());
        enrollment.setCourse(entity.getCourse());
        enrollment.setTerm(entity.getTerm());
        enrollment.setCancellationDeadline(entity.getCancellationDeadline());
        enrollment.setCanceled(entity.isCanceled());
        enrollment.setCourses(entity.getCourses());
        return enrollment;
    }

    public static List<EnrollmentEntity> toEntityList(List<Enrollment> enrollments) {
        return enrollments.stream().map(EnrollmentMapper::toEntity).collect(Collectors.toList());
    }

    public static List<Enrollment> toDomainList(List<EnrollmentEntity> entities) {
        return entities.stream().map(EnrollmentMapper::toDomain).collect(Collectors.toList());
    }
}
