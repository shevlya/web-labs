package ru.ssau.todo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.repository.TaskRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public List<Task> findAll(@RequestParam long userId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to){
        return taskRepository.findAll(from, to, userId);
    }

    @GetMapping("/{id}")
    public Task findById(@PathVariable long id){
        return taskRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    /*@ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody Task task){
        return taskRepository.create(task);
    }*/
    /*
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = taskRepository.create(task);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/tasks/" + created.getId())
                .body(created);
    }*/

    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        try {
            Task created = taskRepository.create(task);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(created.getId())
                    .toUri();
            return ResponseEntity.created(location).body(created);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public void updateTask(@PathVariable long id, @RequestBody Task task) throws Exception{
        task.setId(id);
        try{
            taskRepository.update(task);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable long id){
       taskRepository.deleteById(id);
    }

    @GetMapping("/active/count")
    public long countActive(@RequestParam long userId){
        return taskRepository.countActiveTasksByUserId(userId);
    }
}