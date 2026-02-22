package ru.ssau.todo.entity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum TaskStatus {
    OPEN,
    DONE,
    IN_PROGRESS,
    CLOSED;

    private static final Set<TaskStatus> ACTIVE = EnumSet.of(OPEN, IN_PROGRESS);

    public boolean isActive() {
        return ACTIVE.contains(this);
    }

    public static Set<TaskStatus> getActiveStatuses() {
        return Collections.unmodifiableSet(ACTIVE);
    }
}