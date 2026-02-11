package ru.ssau.todo.repository;

import org.springframework.stereotype.Repository;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class TaskInMemoryRepository implements TaskRepository {
    private final Map<Long, Task> tasks = new HashMap<>();
    private long currentTaskId = 1;

    @Override
    public Task create(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null) {
            throw new IllegalArgumentException();
        }
        task.setId(currentTaskId++);
        task.setCreatedAt(LocalDateTime.now());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        LocalDateTime start = (from != null) ? from : LocalDateTime.MIN;
        LocalDateTime end = (to != null) ? to : LocalDateTime.MAX;
        List<Task> result = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getCreatedBy() == userId) {
                if (task.getCreatedAt() != null && !task.getCreatedAt().isBefore(start) && !task.getCreatedAt().isAfter(end)) {
                    result.add(task);
                }
            }
        }
        return result;
    }

    @Override
    public void update(Task task) {
        Task existingTask = tasks.get(task.getId());
        if (existingTask == null) {
            throw new TaskNotFoundException(task.getId());
        }
        if (task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null) {
            throw new IllegalArgumentException();
        }
        task.setCreatedAt(existingTask.getCreatedAt());
        task.setCreatedBy(existingTask.getCreatedBy());
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteById(long id) {
        tasks.remove(id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        long countActiveTasks = 0;
        for (Task task : tasks.values()) {
            if (task.getCreatedBy() == userId) {
                if (task.getStatus() == TaskStatus.OPEN || task.getStatus() == TaskStatus.IN_PROGRESS) {
                    countActiveTasks++;
                }
            }
        }
        return countActiveTasks;
    }
}
