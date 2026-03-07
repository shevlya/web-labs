package ru.ssau.todo.exception;

public class RoleNotFoundException extends Exception {
    public RoleNotFoundException(String roleName) {
        super("Роль " + roleName + " не найдена");
    }
}
