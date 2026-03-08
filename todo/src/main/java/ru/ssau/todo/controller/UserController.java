package ru.ssau.todo.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ssau.todo.dto.UserDto;
import ru.ssau.todo.exception.RoleNotFoundException;
import ru.ssau.todo.exception.UsernameExistsException;
import ru.ssau.todo.service.CustomUserDetailsService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final CustomUserDetailsService customUserDetailsService;

    public UserController(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody @Valid UserDto userDto) throws UsernameExistsException, RoleNotFoundException {
        return customUserDetailsService.register(userDto);
    }
}