package ru.ssau.srestapp.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import ru.ssau.srestapp.exception.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController()).setControllerAdvice(new ru.ssau.srestapp.controller.GlobalExceptionHandler()).build();
    }

    @Test
    void handleInvalidDateTime_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-date-time"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Дата и время некорректны"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Недопустимый аргумент"));
    }

    @Test
    void handleEventMaxParticipantsReached_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/max-participants"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Достигнуто максимальное количество участников для мероприятия с id=1"));
    }

    @Test
    void handleModerationException_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/moderation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Нет ожидаемых изменений для этого мероприятия"));
    }

    @Test
    void handleEventNotEditable_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/not-editable"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Нельзя редактировать уже начавшееся или завершённое мероприятие"));
    }

    @Test
    void handleInvalidPassword_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Неверный текущий пароль"));
    }

    @Test
    void handlePasswordSameAsOld_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/password-same"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Новый пароль не должен совпадать с текущим"));
    }

    @Test
    void handleEmailAlreadyExists_ShouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test/email-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email уже используется"));
    }

    @Test
    void handleDuplicateEntity_ShouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test/duplicate-entity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Дубликат сущности"));
    }

    @Test
    void handleParticipantAlreadyExists_ShouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test/participant-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Пользователь с id=1 уже записан на мероприятие с id=2"));
    }

    @Test
    void handleEntityNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/test/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Сущность не найдена"));
    }

    @Test
    void handleParticipantNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/test/participant-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Участник с userId=1 не найден на мероприятии с eventId=2"));
    }

    @Test
    void handleAccessDenied_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Доступ запрещён"));
    }

    @Test
    void handleBadCredentials_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Ошибка аутентификации"));
    }

    @Test
    void handleJwtException_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/test/jwt-exception"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Ошибка JWT"));
    }

    @Test
    void handleAuthenticationException_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/test/auth-exception"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Ошибка аутентификации"));
    }

    @Test
    void handleUtilException_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test/util-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ошибка обработки данных: Ошибка в утилите"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/invalid-date-time")
        public void throwInvalidDateTime() throws InvalidDateTimeException {
            throw new InvalidDateTimeException("Дата и время некорректны");
        }

        @GetMapping("/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Недопустимый аргумент");
        }

        @GetMapping("/max-participants")
        public void throwMaxParticipants() throws EventMaxParticipantsReachedException {
            throw new EventMaxParticipantsReachedException(1L);
        }

        @GetMapping("/moderation")
        public void throwModeration() throws ModerationException {
            throw new ModerationException();
        }

        @GetMapping("/not-editable")
        public void throwNotEditable() throws EventNotEditableException {
            throw new EventNotEditableException();
        }

        @GetMapping("/invalid-password")
        public void throwInvalidPassword() throws InvalidPasswordException {
            throw new InvalidPasswordException();
        }

        @GetMapping("/password-same")
        public void throwPasswordSame() throws PasswordSameAsOldException {
            throw new PasswordSameAsOldException();
        }

        @GetMapping("/email-exists")
        public void throwEmailExists() {
            throw new EmailAlreadyExistsException("Email уже используется");
        }

        @GetMapping("/duplicate-entity")
        public void throwDuplicate() throws DuplicateEntityException {
            throw new DuplicateEntityException("Дубликат сущности");
        }

        @GetMapping("/participant-exists")
        public void throwParticipantExists() throws ParticipantAlreadyExistsException {
            throw new ParticipantAlreadyExistsException(1L, 2L);
        }

        @GetMapping("/entity-not-found")
        public void throwEntityNotFound() throws EntityNotFoundException {
            throw new EntityNotFoundException("Сущность не найдена");
        }

        @GetMapping("/participant-not-found")
        public void throwParticipantNotFound() throws ParticipantNotFoundException {
            throw new ParticipantNotFoundException(1L, 2L);
        }

        @GetMapping("/access-denied")
        public void throwAccessDenied() throws AccessDeniedException {
            throw new AccessDeniedException("Доступ запрещён");
        }

        @GetMapping("/bad-credentials")
        public void throwBadCredentials() {
            throw new org.springframework.security.authentication.BadCredentialsException("Неверные учётные данные");
        }

        @GetMapping("/jwt-exception")
        public void throwJwtException() {
            throw new io.jsonwebtoken.JwtException("Ошибка JWT");
        }

        @GetMapping("/auth-exception")
        public void throwAuthException() {
            throw new org.springframework.security.core.AuthenticationException("Ошибка аутентификации") {};
        }

        @GetMapping("/util-exception")
        public void throwUtilException() {
            throw new UtilException("Ошибка в утилите");
        }

        @GetMapping("/generic-exception")
        public void throwGenericException() {
            throw new RuntimeException("Неожиданная ошибка");
        }
    }
}
