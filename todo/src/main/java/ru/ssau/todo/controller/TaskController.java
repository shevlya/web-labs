package ru.ssau.todo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.repository.TaskNotFoundException;
import ru.ssau.todo.service.TaskDeletionNotAllowedException;
import ru.ssau.todo.service.TaskService;
import ru.ssau.todo.service.TooManyActiveTasksException;

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
    public List<Task> findAll(@RequestParam long userId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return taskService.findAll(from, to, userId);
    }

    @GetMapping("/{id}")
    public Task findById(@PathVariable long id) {
        return taskService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            Task created = taskService.createTask(task);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Location", "/tasks/" + created.getId())
                    .body(created);
        } catch (TooManyActiveTasksException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public void updateTask(@PathVariable long id, @RequestBody Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        task.setId(id);
        try {
            taskService.updateTask(task);
        } catch (TaskNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (TooManyActiveTasksException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable long id) {
        try {
            taskService.deleteTask(id);
        } catch (TaskDeletionNotAllowedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (TaskNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/active/count")
    public long countActive(@RequestParam long userId) {
        return taskService.countActive(userId);
    }
}