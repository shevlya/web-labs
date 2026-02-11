package ru.ssau.todo.repository;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(long id){
        super("Задание с id " + id + " не найдено");
    }
}