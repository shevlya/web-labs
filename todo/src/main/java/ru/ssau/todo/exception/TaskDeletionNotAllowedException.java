package ru.ssau.todo.exception;

public class TaskDeletionNotAllowedException extends Exception {
    public TaskDeletionNotAllowedException() {
        super("Нельзя удалить задачу, созданную менее 5 минут назад");
    }
}