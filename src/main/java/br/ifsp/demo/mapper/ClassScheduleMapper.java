package br.ifsp.demo.mapper;

import br.ifsp.demo.domain.ClassSchedule;
import br.ifsp.demo.infrastructure.persistence.ClassScheduleEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ClassScheduleMapper {

    public static ClassScheduleEntity toEntity(ClassSchedule schedule) {
        if (schedule == null) return null;
        ClassScheduleEntity entity = new ClassScheduleEntity();
        entity.setDayOfWeek(schedule.getDayOfWeek());
        entity.setStartTime(schedule.getStartTime());
        entity.setEndTime(schedule.getEndTime());
        return entity;
    }

    public static ClassSchedule toDomain(ClassScheduleEntity entity) {
        if (entity == null) return null;
        ClassSchedule schedule = new ClassSchedule();
        schedule.setDayOfWeek(entity.getDayOfWeek());
        schedule.setStartTime(entity.getStartTime());
        schedule.setEndTime(entity.getEndTime());
        return schedule;
    }

    public static List<ClassScheduleEntity> toEntityList(List<ClassSchedule> schedules) {
        return schedules.stream().map(ClassScheduleMapper::toEntity).collect(Collectors.toList());
    }

    public static List<ClassSchedule> toDomainList(List<ClassScheduleEntity> entities) {
        return entities.stream().map(ClassScheduleMapper::toDomain).collect(Collectors.toList());
    }
}
