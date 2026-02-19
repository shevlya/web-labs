package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskDeletionNotAllowedException;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.exception.TooManyActiveTasksException;
import ru.ssau.todo.repository.TaskRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private static final int MAX_ACTIVE_TASKS = 10;
    private static final int MIN_DELETE_MINUTES = 1;

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        return taskRepository.findAll(from, to, userId);
    }

    public Task createTask(Task task) throws TooManyActiveTasksException {
        validateActiveLimit(task.getCreatedBy(), task.getStatus());
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.create(task);
    }

    public Task update(long id, Task updatedTask) throws TaskNotFoundException, TooManyActiveTasksException {
        Task existing = getByIdOrThrow(id);
        updatedTask.setId(id);
        updatedTask.setCreatedBy(existing.getCreatedBy());
        updatedTask.setCreatedAt(existing.getCreatedAt());
        validateActiveLimitOnUpdate(existing, updatedTask);
        taskRepository.update(updatedTask);
        return updatedTask;
    }

    public void deleteTask(long id) throws TaskNotFoundException, TaskDeletionNotAllowedException {
        Task task = getByIdOrThrow(id);
        long minutesSinceCreation = Duration.between(task.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutesSinceCreation < MIN_DELETE_MINUTES) {
            throw new TaskDeletionNotAllowedException(MIN_DELETE_MINUTES);
        }
        taskRepository.deleteById(id);
    }

    public long countActive(long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }

    private void validateActiveLimit(long userId, TaskStatus status) throws TooManyActiveTasksException {
        if (!isActive(status)) return;
        if (taskRepository.countActiveTasksByUserId(userId) >= MAX_ACTIVE_TASKS) {
            throw new TooManyActiveTasksException(userId);
        }
    }

    private void validateActiveLimitOnUpdate(Task oldTask, Task newTask) throws TooManyActiveTasksException {
        if (isActive(newTask.getStatus()) && !isActive(oldTask.getStatus())) {
            validateActiveLimit(oldTask.getCreatedBy(), newTask.getStatus());
        }
    }

    private boolean isActive(TaskStatus status) {
        return status.isActive();
    }

    public Task getByIdOrThrow(long id) throws TaskNotFoundException {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
