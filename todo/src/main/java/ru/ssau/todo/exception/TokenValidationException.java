package ru.ssau.todo.exception;

public class TokenValidationException extends Exception {
    public TokenValidationException(String message) {
        super(message);
    }
}
