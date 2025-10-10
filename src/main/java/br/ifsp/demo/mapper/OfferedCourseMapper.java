package br.ifsp.demo.mapper;

import br.ifsp.demo.domain.OfferedCourse;
import br.ifsp.demo.infrastructure.persistence.OfferedCourseEntity;

import java.util.List;
import java.util.stream.Collectors;

public class OfferedCourseMapper {

    public static OfferedCourseEntity toEntity(OfferedCourse course) {
        if (course == null) return null;
        OfferedCourseEntity entity = new OfferedCourseEntity();
        entity.setCode(course.getCode());
        entity.setName(course.getName());
        entity.setCredits(course.getCredits());
        entity.setAvailableSeats(course.getAvailableSeats());
        entity.setSchedule(ClassScheduleMapper.toEntityList(course.getSchedule()));
        entity.setPrerequisites(course.getPrerequisites());
        entity.setTerm(course.getTerm());
        return entity;
    }

    public static OfferedCourse toDomain(OfferedCourseEntity entity) {
        if (entity == null) return null;
        OfferedCourse course = new OfferedCourse(entity.getCode(), entity.getName(), entity.getCredits());
        course.setAvailableSeats(entity.getAvailableSeats());
        course.setSchedule(ClassScheduleMapper.toDomainList(entity.getSchedule()));
        course.setPrerequisites(entity.getPrerequisites());
        course.setTerm(entity.getTerm());
        return course;
    }

    public static List<OfferedCourseEntity> toEntityList(List<OfferedCourse> courses) {
        return courses.stream().map(OfferedCourseMapper::toEntity).collect(Collectors.toList());
    }

    public static List<OfferedCourse> toDomainList(List<OfferedCourseEntity> entities) {
        return entities.stream().map(OfferedCourseMapper::toDomain).collect(Collectors.toList());
    }
}
