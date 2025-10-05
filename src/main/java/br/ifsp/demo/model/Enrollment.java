package br.ifsp.demo.model;

public class Enrollment {

    private final String courseName;
    private final String schedule;
    private final int credits;
    private final int vacancies;

    public Enrollment(String courseName, String schedule, int credits, int vacancies) {
        this.courseName = courseName;
        this.schedule = schedule;
        this.credits = credits;
        this.vacancies = vacancies;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getSchedule() {
        return schedule;
    }

    public int getCredits() {
        return credits;
    }

    public int getVacancies() {
        return vacancies;
    }
}

