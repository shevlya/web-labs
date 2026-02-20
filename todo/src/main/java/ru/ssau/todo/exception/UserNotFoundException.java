package ru.ssau.todo.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(Long id) {
        super("Пользователь с id " + id + " не найден");
    }
}