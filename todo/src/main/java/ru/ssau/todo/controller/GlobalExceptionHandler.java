package ru.ssau.todo.controller;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.ssau.todo.exception.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private Map<String, Object> buildResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Ошибка валидации");
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({TaskNotFoundException.class, UserNotFoundByIdException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(Exception e) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({TooManyActiveTasksException.class, TaskDeletionNotAllowedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadRequest(Exception e) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /*
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleDataIntegrity(DataIntegrityViolationException e) {
        if (e.getMessage().contains("fk_created_by") || e.getMessage().contains("user_account")) {
            return buildResponse("Указанный пользователь не существует", HttpStatus.NOT_FOUND);
        }
        return buildResponse("Ошибка целостности данных", HttpStatus.NOT_FOUND);
    }*/

    @ExceptionHandler(UsernameExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleUsernameExists(UsernameExistsException e) {
        return buildResponse(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleRoleNotFound(RoleNotFoundException e) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenValidationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleTokenValidation(TokenValidationException e) {
        return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleAll() {
        return buildResponse("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}