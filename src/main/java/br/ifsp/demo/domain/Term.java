package br.ifsp.demo.domain;

import jakarta.persistence.Embeddable;
import java.time.Year;

@Embeddable
public class Term {
    private int year;
    private int semester;

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    protected Term() {}

    public Term(int year, int semester) {
        this.year = year;
        this.semester = semester;
    }

    public static Term current() {
        int currentSemester = (java.time.LocalDate.now().getMonthValue() <= 6) ? 1 : 2;
        return new Term(Year.now().getValue(), currentSemester);
    }

@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Term)) return false;
        Term term = (Term) o;
        return year == term.year && semester == term.semester;
    }

    @Override
    public int hashCode() {
        return 31 * year + semester;
    }
}
