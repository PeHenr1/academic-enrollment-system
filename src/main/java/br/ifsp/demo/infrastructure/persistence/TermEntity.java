package br.ifsp.demo.infrastructure.persistence;

import jakarta.persistence.Embeddable;

@Embeddable
public class TermEntity {

    private int year;
    private int semester;

    public TermEntity() {}

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
}
