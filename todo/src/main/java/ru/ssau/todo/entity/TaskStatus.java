package ru.ssau.todo.entity;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum TaskStatus {
    OPEN,
    DONE,
    IN_PROGRESS,
    CLOSED;

    private static final Set<TaskStatus> ACTIVE = EnumSet.of(OPEN, IN_PROGRESS);

    public boolean isActive() {
        return ACTIVE.contains(this);
    }

    public static Set<String> getActiveStatusNames() {
        return ACTIVE.stream().map(Enum::name).collect(Collectors.toSet());
    }
}