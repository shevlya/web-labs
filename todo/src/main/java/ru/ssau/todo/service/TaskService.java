package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.exception.TaskDeletionNotAllowedException;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.exception.TooManyActiveTasksException;
import ru.ssau.todo.exception.UserNotFoundException;
import ru.ssau.todo.repository.TaskRepository;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.utils.TaskMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final int MAX_ACTIVE_TASKS = 10;
    private static final int MIN_DELETE_MINUTES = 5;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskDto> findAll(LocalDateTime from, LocalDateTime to, Long userId) {
        return taskRepository.findAllByUserIdAndDateRange(userId, from, to)
                .stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDto getById(Long id) throws TaskNotFoundException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return TaskMapper.toDto(task);
    }

    @Transactional
    public TaskDto createTask(TaskDto taskDto) throws TooManyActiveTasksException, UserNotFoundException {
        Long userId = taskDto.getCreatedBy();
        validateActiveLimit(userId, taskDto.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Task task = TaskMapper.toEntity(taskDto);
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return TaskMapper.toDto(saved);
    }

    @Transactional
    public TaskDto update(Long id, TaskDto taskDto) throws TaskNotFoundException, TooManyActiveTasksException {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        validateActiveLimitOnUpdate(existing, taskDto.getStatus());

        existing.setTitle(taskDto.getTitle());
        existing.setStatus(taskDto.getStatus());

        Task updated = taskRepository.save(existing);
        return TaskMapper.toDto(updated);
    }

    @Transactional
    public void deleteTask(Long id) throws TaskNotFoundException, TaskDeletionNotAllowedException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        long minutes = Duration.between(task.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutes < MIN_DELETE_MINUTES) {
            throw new TaskDeletionNotAllowedException(MIN_DELETE_MINUTES);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countActive(Long userId) {
        return taskRepository.countActiveByUserId(userId);
    }

    private void validateActiveLimit(Long userId, TaskStatus status) throws TooManyActiveTasksException {
        if (!status.isActive()) return;
        if (taskRepository.countActiveByUserId(userId) >= MAX_ACTIVE_TASKS) {
            throw new TooManyActiveTasksException(userId);
        }
    }

    private void validateActiveLimitOnUpdate(Task existing, TaskStatus newStatus) throws TooManyActiveTasksException {
        if (newStatus.isActive() && !existing.getStatus().isActive()) {
            validateActiveLimit(existing.getCreatedBy().getId(), newStatus);
        }
    }
}