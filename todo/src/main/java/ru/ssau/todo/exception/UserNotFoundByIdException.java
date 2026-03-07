package ru.ssau.todo.exception;

public class UserNotFoundByIdException extends Exception {
    public UserNotFoundByIdException(Long id) {
        super("Пользователь с id " + id + " не найден");
    }
}