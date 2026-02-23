package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.exception.*;
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
        return taskRepository.findAll(userId, from, to)
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
    public TaskDto createTask(TaskDto dto) throws TooManyActiveTasksException, UserNotFoundException {
        Long userId = dto.getCreatedBy();
        validateActiveLimit(userId, dto.getStatus());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Task task = TaskMapper.toEntity(dto);
        task.setCreatedBy(user);
        return TaskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto update(Long id, TaskDto dto) throws TaskNotFoundException, TooManyActiveTasksException {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        validateActiveLimitOnUpdate(existing, dto.getStatus());
        existing.setTitle(dto.getTitle());
        existing.setStatus(dto.getStatus());
        return TaskMapper.toDto(taskRepository.save(existing));
    }

    @Transactional
    public void deleteTask(Long id) throws TaskNotFoundException, TaskDeletionNotAllowedException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        long minutes = Duration.between(task.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutes < MIN_DELETE_MINUTES) {
            throw new TaskDeletionNotAllowedException(MIN_DELETE_MINUTES);
        }

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public long countActive(Long userId) {
        return taskRepository.countActiveByUserId(userId, TaskStatus.getActiveStatuses()
        );
    }

    private void validateActiveLimit(Long userId, TaskStatus status) throws TooManyActiveTasksException {
        if (!status.isActive()) return;
        if (countActive(userId) >= MAX_ACTIVE_TASKS) {
            throw new TooManyActiveTasksException(userId);
        }
    }

    private void validateActiveLimitOnUpdate(Task existing, TaskStatus newStatus)
            throws TooManyActiveTasksException {
        if (newStatus.isActive() && !existing.getStatus().isActive()) {
            validateActiveLimit(existing.getCreatedBy().getId(), newStatus);
        }
    }
}