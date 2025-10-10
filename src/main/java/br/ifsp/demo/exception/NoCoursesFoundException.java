package br.ifsp.demo.exception;

public class NoCoursesFoundException extends RuntimeException {
  public NoCoursesFoundException(String message) {
    super(message);
  }
}
