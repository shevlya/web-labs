package ru.ssau.todo.service;

public class TooManyActiveTasksException extends RuntimeException {
    public TooManyActiveTasksException(long userId) {
        super("Пользователь с id " + userId + " не может иметь более 10 активных задач");
    }
}
