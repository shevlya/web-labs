package ru.ssau.todo.utils;

import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.entity.Task;

public class TaskMapper {
    public static TaskDto toDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setCreatedBy(task.getCreatedBy().getId());
        return dto;
    }

    public static Task toEntity(TaskDto dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setStatus(dto.getStatus());
        return task;
    }
}