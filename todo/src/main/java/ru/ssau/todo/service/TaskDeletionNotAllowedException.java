package ru.ssau.todo.service;

public class TaskDeletionNotAllowedException extends RuntimeException {
    public TaskDeletionNotAllowedException() {
        super("Нельзя удалить задачу, созданную менее 5 минут назад");
    }
}