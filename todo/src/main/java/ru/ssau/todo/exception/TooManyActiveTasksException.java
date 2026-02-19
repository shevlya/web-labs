package ru.ssau.todo.exception;

public class TooManyActiveTasksException extends Exception {
    public TooManyActiveTasksException(long userId) {
        super("Превышен лимит активных задач для пользователя " + userId);
    }
}