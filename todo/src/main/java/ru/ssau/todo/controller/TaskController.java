package ru.ssau.todo.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.exception.TaskDeletionNotAllowedException;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.exception.TooManyActiveTasksException;
import ru.ssau.todo.exception.UserNotFoundException;
import ru.ssau.todo.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskDto> findAll(@RequestParam Long userId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return taskService.findAll(from, to, userId);
    }

    @GetMapping("/{id}")
    public TaskDto findById(@PathVariable Long id) throws TaskNotFoundException {
        return taskService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto createTask(@RequestBody @Valid TaskDto taskDto) throws TooManyActiveTasksException, UserNotFoundException {
        return taskService.createTask(taskDto);
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable Long id, @RequestBody @Valid TaskDto taskDto) throws TaskNotFoundException, TooManyActiveTasksException {
        return taskService.update(id, taskDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) throws TaskNotFoundException, TaskDeletionNotAllowedException {
        taskService.deleteTask(id);
    }

    @GetMapping("/active/count")
    public long countActive(@RequestParam Long userId) {
        return taskService.countActive(userId);
    }
}