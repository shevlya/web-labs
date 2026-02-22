package ru.ssau.todo.utils;

import ru.ssau.todo.dto.UserDto;
import ru.ssau.todo.entity.User;

public class UserMapper {
    public static UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getUsername()
        );
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        return user;
    }
}