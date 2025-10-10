package br.ifsp.demo.mapper;

import br.ifsp.demo.domain.Course;
import br.ifsp.demo.infrastructure.persistence.CourseEntity;
import br.ifsp.demo.mapper.ClassScheduleMapper;

import java.util.List;
import java.util.stream.Collectors;

public class CourseMapper {

    public static CourseEntity toEntity(Course course) {
        if (course == null) return null;
        CourseEntity entity = new CourseEntity();
        entity.setId(course.getId());
        entity.setCode(course.getCode());
        entity.setName(course.getName());
        entity.setCredits(course.getCredits());
        entity.setAvailableSeats(course.getAvailableSeats());
        entity.setShift(course.getShift());
        entity.setSchedule(ClassScheduleMapper.toEntityList(course.getSchedule()));
        entity.setPrerequisites(course.getPrerequisites());
        entity.setTerm(course.getTerm());
        entity.setEnrollment(EnrollmentMapper.toEntity(course.getEnrollment()));
        return entity;
    }

    public static Course toDomain(CourseEntity entity) {
        if (entity == null) return null;
        Course course = new Course();
        course.setId(entity.getId());
        course.setCode(entity.getCode());
        course.setName(entity.getName());
        course.setCredits(entity.getCredits());
        course.setAvailableSeats(entity.getAvailableSeats());
        course.setShift(entity.getShift());
        course.setSchedule(ClassScheduleMapper.toDomainList(entity.getSchedule()));
        course.setPrerequisites(entity.getPrerequisites());
        course.setTerm(entity.getTerm());
        course.setEnrollment(EnrollmentMapper.toDomain(entity.getEnrollment()));
        return course;
    }

    public static List<CourseEntity> toEntityList(List<Course> courses) {
        return courses.stream().map(CourseMapper::toEntity).collect(Collectors.toList());
    }

    public static List<Course> toDomainList(List<CourseEntity> entities) {
        return entities.stream().map(CourseMapper::toDomain).collect(Collectors.toList());
    }
}
