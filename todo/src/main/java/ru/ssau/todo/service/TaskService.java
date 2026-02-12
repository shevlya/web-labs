package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.repository.TaskNotFoundException;
import ru.ssau.todo.repository.TaskRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        return taskRepository.findAll(from, to, userId);
    }

    public Optional<Task> findById(long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        long activeCount = taskRepository.countActiveTasksByUserId(task.getCreatedBy());
        if (task.getStatus() == TaskStatus.OPEN || task.getStatus() == TaskStatus.IN_PROGRESS) {
            if (activeCount >= 10) {
                throw new IllegalStateException("Пользователь не может иметь более 10 активных задач " + task.getCreatedBy());
            }
        }
        return taskRepository.create(task);
    }

    public void updateTask(Task task) {
        Task existing = taskRepository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException(task.getId()));
        task.setCreatedBy(existing.getCreatedBy());
        task.setCreatedAt(existing.getCreatedAt());
        if ((task.getStatus() == TaskStatus.OPEN || task.getStatus() == TaskStatus.IN_PROGRESS) && !(existing.getStatus() == TaskStatus.OPEN || existing.getStatus() == TaskStatus.IN_PROGRESS)) {
            long activeCount = taskRepository.countActiveTasksByUserId(existing.getCreatedBy());
            if (activeCount >= 10) {
                throw new IllegalStateException("Пользователь не может иметь более 10 активных задач");
            }
        }
        taskRepository.update(task);
    }

    public void deleteTask(long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getCreatedAt() != null) {
            long minutesSinceCreation = Duration.between(task.getCreatedAt(), LocalDateTime.now()).toMinutes();
            if (minutesSinceCreation < 5) {
                throw new IllegalStateException("Нельзя удалить задачу созданную менее 5 минут назад");
            }
        }
        taskRepository.deleteById(id);
    }

    public long countActive(long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }
}
