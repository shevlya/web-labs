package ru.ssau.todo.repository;

import org.springframework.stereotype.Repository;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskInMemoryRepository implements TaskRepository{
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
        return tasks.values().stream()
                .filter(task -> task.getCreatedBy() == userId)
                .filter(task -> !task.getCreatedAt().isBefore(start) && !task.getCreatedAt().isAfter(end))
                .toList();
    }

    /*@Override
    public void update(Task task) throws Exception {
        if(!tasks.containsKey(task.getId())){
            throw new Exception();
        }
        tasks.put(task.getId(), task);
    }*/

    @Override
    public void update(Task task) throws Exception {
        Task existingTask = tasks.get(task.getId());
        if (existingTask == null) {
            throw new Exception();
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
        return tasks.values().stream()
                .filter(task -> task.getCreatedBy() == userId)
                .filter(task -> task.getStatus() == TaskStatus.OPEN || task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
    }
}
