package ru.ssau.srestapp.dto.avatar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AvatarRequestDto {

    @NotBlank(message = "URL аватара обязателен")
    private String avatarUrl;
}
