package ru.ssau.todo.exception;

public class TaskDeletionNotAllowedException extends Exception {
    public TaskDeletionNotAllowedException(int minutes) {
        super("Нельзя удалить задачу, созданную менее " + minutes + " минут назад");
    }
}