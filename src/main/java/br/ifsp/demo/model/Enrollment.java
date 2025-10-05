package br.ifsp.demo.model;

public class Enrollment {

    private Long id;
    private boolean canceled = false;

    public Enrollment(Long id) {
        this.id = id;
    }

    public void cancel() {
        this.canceled = true;
    }
}
