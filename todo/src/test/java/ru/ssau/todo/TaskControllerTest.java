package ru.ssau.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ssau.todo.controller.TaskController;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskDeletionNotAllowedException;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.exception.TooManyActiveTasksException;
import ru.ssau.todo.service.TaskService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    // Константы для идентификаторов
    private static final Long USER_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long TASK_ID = 1L;
    private static final Long YOUNG_TASK_ID = 2L;

    // Константы для названий
    private static final String TEST_TITLE = "Test";
    private static final String NEW_TASK_TITLE = "New Task";
    private static final String UPDATED_TITLE = "Updated";
    private static final String TASK_TITLE = "Task";

    // Константы для статусов
    private static final TaskStatus OPEN_STATUS = TaskStatus.OPEN;
    private static final TaskStatus DONE_STATUS = TaskStatus.DONE;

    // Константы для количества
    private static final long ACTIVE_COUNT = 5;

    // Константы для минут удаления
    private static final int MIN_DELETE_MINUTES = 5;

    // Базовый URL
    private static final String BASE_URL = "/tasks";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findAll_ShouldReturnListOfTasks() throws Exception {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(TASK_ID);
        taskDto.setTitle(TEST_TITLE);
        taskDto.setStatus(OPEN_STATUS);
        taskDto.setCreatedBy(USER_ID);
        taskDto.setCreatedAt(LocalDateTime.now());

        when(taskService.findAll(any(), any(), eq(USER_ID)))
                .thenReturn(List.of(taskDto));

        mockMvc.perform(get(BASE_URL)
                        .param("userId", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TASK_ID))
                .andExpect(jsonPath("$[0].title").value(TEST_TITLE))
                .andExpect(jsonPath("$[0].status").value(OPEN_STATUS.name()));
    }

    @Test
    void findById_ExistingId_ShouldReturnTask() throws Exception {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(TASK_ID);
        taskDto.setTitle(TEST_TITLE);
        taskDto.setStatus(OPEN_STATUS);

        when(taskService.getById(TASK_ID)).thenReturn(taskDto);

        mockMvc.perform(get(BASE_URL + "/{id}", TASK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.title").value(TEST_TITLE));
    }

    @Test
    void findById_NonExistingId_ShouldReturn404() throws Exception {
        when(taskService.getById(NON_EXISTENT_ID)).thenThrow(new TaskNotFoundException(NON_EXISTENT_ID));

        mockMvc.perform(get(BASE_URL + "/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Задача с id " + NON_EXISTENT_ID + " не найдена"));
    }

    @Test
    void createTask_ValidDto_ShouldReturn201() throws Exception {
        TaskDto requestDto = new TaskDto();
        requestDto.setTitle(NEW_TASK_TITLE);
        requestDto.setStatus(OPEN_STATUS);
        requestDto.setCreatedBy(USER_ID);

        TaskDto responseDto = new TaskDto();
        responseDto.setId(TASK_ID);
        responseDto.setTitle(NEW_TASK_TITLE);
        responseDto.setStatus(OPEN_STATUS);
        responseDto.setCreatedBy(USER_ID);
        responseDto.setCreatedAt(LocalDateTime.now());

        when(taskService.createTask(any(TaskDto.class))).thenReturn(responseDto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.title").value(NEW_TASK_TITLE));
    }

    @Test
    void createTask_InvalidDto_ShouldReturn400() throws Exception {
        TaskDto invalidDto = new TaskDto();
        invalidDto.setTitle(""); // пустой заголовок
        invalidDto.setStatus(OPEN_STATUS);
        invalidDto.setCreatedBy(USER_ID);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_TooManyActive_ShouldReturn400() throws Exception {
        TaskDto requestDto = new TaskDto();
        requestDto.setTitle(TASK_TITLE);
        requestDto.setStatus(OPEN_STATUS);
        requestDto.setCreatedBy(USER_ID);

        when(taskService.createTask(any(TaskDto.class)))
                .thenThrow(new TooManyActiveTasksException(USER_ID));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Превышен лимит активных задач для пользователя " + USER_ID));
    }

    @Test
    void updateTask_ValidDto_ShouldReturn200() throws Exception {
        TaskDto updateDto = new TaskDto();
        updateDto.setTitle(UPDATED_TITLE);
        updateDto.setStatus(DONE_STATUS);
        updateDto.setCreatedBy(USER_ID); // для валидации

        TaskDto responseDto = new TaskDto();
        responseDto.setId(TASK_ID);
        responseDto.setTitle(UPDATED_TITLE);
        responseDto.setStatus(DONE_STATUS);
        responseDto.setCreatedBy(USER_ID);

        when(taskService.update(eq(TASK_ID), any(TaskDto.class))).thenReturn(responseDto);

        mockMvc.perform(put(BASE_URL + "/{id}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(UPDATED_TITLE))
                .andExpect(jsonPath("$.status").value(DONE_STATUS.name()));
    }

    @Test
    void updateTask_NonExistingId_ShouldReturn404() throws Exception {
        TaskDto updateDto = new TaskDto();
        updateDto.setTitle(UPDATED_TITLE);
        updateDto.setStatus(DONE_STATUS);
        updateDto.setCreatedBy(USER_ID); // для валидации

        when(taskService.update(eq(NON_EXISTENT_ID), any(TaskDto.class)))
                .thenThrow(new TaskNotFoundException(NON_EXISTENT_ID));

        mockMvc.perform(put(BASE_URL + "/{id}", NON_EXISTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Задача с id " + NON_EXISTENT_ID + " не найдена"));
    }

    @Test
    void deleteTask_ExistingId_ShouldReturn204() throws Exception {
        doNothing().when(taskService).deleteTask(TASK_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", TASK_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_NonExistingId_ShouldReturn404() throws Exception {
        doThrow(new TaskNotFoundException(NON_EXISTENT_ID)).when(taskService).deleteTask(NON_EXISTENT_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Задача с id " + NON_EXISTENT_ID + " не найдена"));
    }

    @Test
    void deleteTask_TooYoung_ShouldReturn400() throws Exception {
        doThrow(new TaskDeletionNotAllowedException(MIN_DELETE_MINUTES)).when(taskService).deleteTask(YOUNG_TASK_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", YOUNG_TASK_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Нельзя удалить задачу, созданную менее " + MIN_DELETE_MINUTES + " минут назад"));
    }

    @Test
    void countActive_ShouldReturnCount() throws Exception {
        when(taskService.countActive(USER_ID)).thenReturn(ACTIVE_COUNT);

        mockMvc.perform(get(BASE_URL + "/active/count")
                        .param("userId", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(ACTIVE_COUNT)));
    }
}