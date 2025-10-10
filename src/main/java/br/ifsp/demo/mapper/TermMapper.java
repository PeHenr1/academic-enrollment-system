package br.ifsp.demo.mapper;

import br.ifsp.demo.domain.Term;
import br.ifsp.demo.infrastructure.persistence.TermEntity;

public class TermMapper {

    public static TermEntity toEntity(Term term) {
        if (term == null) return null;
        TermEntity entity = new TermEntity();
        entity.setYear(term.getYear());
        entity.setSemester(term.getSemester());
        return entity;
    }

    public static Term toDomain(TermEntity entity) {
        if (entity == null) return null;
        Term term = new Term();
        term.setYear(entity.getYear());
        term.setSemester(entity.getSemester());
        return term;
    }
}
