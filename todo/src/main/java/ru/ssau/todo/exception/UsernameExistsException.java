package ru.ssau.todo.exception;

public class UsernameExistsException extends Exception {
    public UsernameExistsException() {
        super("Такое имя пользователя уже существует");
    }
}