package br.ifsp.demo.domain;

import java.time.Year;

public class Term {

    private final int year;
    private final int semester; // 1 or 2

    public Term(int year, int semester) {
        this.year = year;
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public int getSemester() {
        return semester;
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
