package ru.ssau.todo.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.exception.TaskNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@Profile("in-memory")
public class TaskInMemoryRepository implements TaskRepository {
    private final Map<Long, Task> tasks = new HashMap<>();
    private long currentTaskId = 1;

    @Override
    public Task create(Task task) {
        task.setId(currentTaskId++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getCreatedBy() == userId) {
                if (task.getCreatedAt() != null && !task.getCreatedAt().isBefore(from) && !task.getCreatedAt().isAfter(to)) {
                    result.add(task);
                }
            }
        }
        return result;
    }

    @Override
    /*public void update(Task task) throws TaskNotFoundException {
        Task existingTask = tasks.get(task.getId());
        if (existingTask == null) {
            throw new TaskNotFoundException(task.getId());
        }
        task.setCreatedAt(existingTask.getCreatedAt());
        task.setCreatedBy(existingTask.getCreatedBy());
        tasks.put(task.getId(), task);
    }*/

    public void update(Task task) throws TaskNotFoundException {
        if (!tasks.containsKey(task.getId())) {
            throw new TaskNotFoundException(task.getId());
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteById(long id) throws TaskNotFoundException {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }
        tasks.remove(id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        long countActiveTasks = 0;
        for (Task task : tasks.values()) {
            if (task.getCreatedBy() == userId) {
                if (task.getStatus().isActive()) {
                    countActiveTasks++;
                }
            }
        }
        return countActiveTasks;
    }
}
