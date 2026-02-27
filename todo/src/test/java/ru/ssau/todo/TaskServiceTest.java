package ru.ssau.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.exception.*;
import ru.ssau.todo.repository.TaskRepository;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    // Константы для идентификаторов
    private static final Long USER_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long TASK_ID = 1L;
    private static final Long ANOTHER_TASK_ID = 2L;

    // Константы для количества задач
    private static final int ACTIVE_LIMIT = 10;
    private static final int ACTIVE_COUNT = 5; // количество активных задач до лимита
    private static final long EXPECTED_ACTIVE_COUNT = 3;

    // Константы для времени (минуты)
    private static final int OLD_TASK_MINUTES = 10;
    private static final int YOUNG_TASK_MINUTES = 1;

    // Константы для названий
    private static final String USERNAME = "testuser";
    private static final String TASK_TITLE = "Test Task";
    private static final String NEW_TASK_TITLE = "New Task";
    private static final String OLD_TITLE = "Old";
    private static final String UPDATED_TITLE = "Updated";
    private static final String YOUNG_TASK_TITLE = "Young";

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TaskService taskService;

    private final User testUser = createUser(USER_ID, USERNAME);
    private final Task testTask = createTask(TASK_ID, TASK_TITLE, TaskStatus.OPEN, testUser);

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setUsername(name);
        return user;
    }

    private Task createTask(Long id, String title, TaskStatus status, User user) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setStatus(status);
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    private TaskDto createTaskDto(Long id, String title, TaskStatus status, Long userId) {
        TaskDto dto = new TaskDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setStatus(status);
        dto.setCreatedBy(userId);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    @Test
    void findAll_ShouldReturnList() {
        when(taskRepository.findAllByUserAndDateRange(eq(USER_ID), any(), any()))
                .thenReturn(List.of(testTask));
        List<TaskDto> result = taskService.findAll(null, null, USER_ID);
        assertEquals(1, result.size());
        assertEquals(testTask.getId(), result.get(0).getId());
        verify(taskRepository).findAllByUserAndDateRange(USER_ID, null, null);
    }

    @Test
    void getById_ShouldReturnTask() throws Exception {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
        TaskDto result = taskService.getById(TASK_ID);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTitle(), result.getTitle());
    }

    @Test
    void getById_NotFound_ShouldThrow() {
        when(taskRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.getById(NON_EXISTENT_ID));
    }

    @Test
    void createTask_ShouldSucceed() throws Exception {
        TaskDto dto = createTaskDto(null, NEW_TASK_TITLE, TaskStatus.OPEN, USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(taskRepository.countActiveByUserId(eq(USER_ID), anySet())).thenReturn((long) ACTIVE_COUNT);
        Task saved = createTask(TASK_ID, NEW_TASK_TITLE, TaskStatus.OPEN, testUser);
        when(taskRepository.save(any())).thenReturn(saved);

        TaskDto result = taskService.createTask(dto);
        assertEquals(TASK_ID, result.getId());
        assertEquals(NEW_TASK_TITLE, result.getTitle());
        verify(userRepository).findById(USER_ID);
        verify(taskRepository).save(any());
    }

    @Test
    void createTask_UserNotFound_ShouldThrow() {
        TaskDto dto = createTaskDto(null, NEW_TASK_TITLE, TaskStatus.OPEN, NON_EXISTENT_ID);
        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> taskService.createTask(dto));
    }

    @Test
    void createTask_ActiveLimitExceeded_ShouldThrow() {
        TaskDto dto = createTaskDto(null, NEW_TASK_TITLE, TaskStatus.OPEN, USER_ID);
        when(taskRepository.countActiveByUserId(eq(USER_ID), anySet())).thenReturn((long) ACTIVE_LIMIT);
        assertThrows(TooManyActiveTasksException.class, () -> taskService.createTask(dto));
    }

    @Test
    void update_ShouldModifyTask() throws Exception {
        TaskDto updateDto = createTaskDto(null, UPDATED_TITLE, TaskStatus.DONE, null);
        Task existing = createTask(TASK_ID, OLD_TITLE, TaskStatus.OPEN, testUser);
        Task updated = createTask(TASK_ID, UPDATED_TITLE, TaskStatus.DONE, testUser);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any())).thenReturn(updated);

        TaskDto result = taskService.update(TASK_ID, updateDto);
        assertEquals(UPDATED_TITLE, result.getTitle());
        assertEquals(TaskStatus.DONE, result.getStatus());
        verify(taskRepository).save(any());
    }

    @Test
    void update_NonExisting_ShouldThrow() {
        when(taskRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        TaskDto dto = createTaskDto(null, UPDATED_TITLE, TaskStatus.DONE, null);
        assertThrows(TaskNotFoundException.class, () -> taskService.update(NON_EXISTENT_ID, dto));
    }

    @Test
    void update_TransitionToActiveWithLimit_ShouldThrow() {
        TaskDto dto = createTaskDto(null, null, TaskStatus.IN_PROGRESS, null);
        Task existing = createTask(TASK_ID, OLD_TITLE, TaskStatus.DONE, testUser);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existing));
        when(taskRepository.countActiveByUserId(eq(USER_ID), anySet())).thenReturn((long) ACTIVE_LIMIT);
        assertThrows(TooManyActiveTasksException.class, () -> taskService.update(TASK_ID, dto));
    }

    @Test
    void deleteTask_OldEnough_ShouldDelete() throws Exception {
        Task old = createTask(TASK_ID, OLD_TITLE, TaskStatus.DONE, testUser);
        old.setCreatedAt(LocalDateTime.now().minusMinutes(OLD_TASK_MINUTES));
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(old));
        taskService.deleteTask(TASK_ID);
        verify(taskRepository).delete(old);
    }

    @Test
    void deleteTask_TooYoung_ShouldThrow() {
        Task young = createTask(ANOTHER_TASK_ID, YOUNG_TASK_TITLE, TaskStatus.OPEN, testUser);
        young.setCreatedAt(LocalDateTime.now().minusMinutes(YOUNG_TASK_MINUTES));
        when(taskRepository.findById(ANOTHER_TASK_ID)).thenReturn(Optional.of(young));
        assertThrows(TaskDeletionNotAllowedException.class, () -> taskService.deleteTask(ANOTHER_TASK_ID));
    }

    @Test
    void deleteTask_NotFound_ShouldThrow() {
        when(taskRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(NON_EXISTENT_ID));
    }

    @Test
    void countActive_ShouldReturnCount() {
        when(taskRepository.countActiveByUserId(eq(USER_ID), anySet())).thenReturn(EXPECTED_ACTIVE_COUNT);
        assertEquals(EXPECTED_ACTIVE_COUNT, taskService.countActive(USER_ID));
    }
}