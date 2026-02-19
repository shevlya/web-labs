package ru.ssau.todo.exception;

public class TaskNotFoundException extends Exception {
    public TaskNotFoundException(long id) {
        super("Задача с id " + id + " не найдена");
    }
}