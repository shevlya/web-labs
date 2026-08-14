package ru.ssau.srestapp.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.srestapp.controller.EventCategoryController;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryRequestDto;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryResponseDto;
import ru.ssau.srestapp.exception.DuplicateEntityException;
import ru.ssau.srestapp.exception.EntityNotFoundException;
import ru.ssau.srestapp.service.EventCategoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCategoryControllerTest {

    @Mock
    private EventCategoryService eventCategoryService;

    @InjectMocks
    private EventCategoryController eventCategoryController;

    private EventCategoryResponseDto responseDto;
    private EventCategoryRequestDto requestDto;

    @BeforeEach
    void setUp() {
        responseDto = new EventCategoryResponseDto(1L, "Концерт", "Живая музыка", "#FF5733");
        requestDto = new EventCategoryRequestDto();
        requestDto.setEventCategoryName("Концерт");
        requestDto.setEventCategoryDescription("Живая музыка");
        requestDto.setColorCode("#FF5733");
    }

    //для getAll()
    @Test
    void testGetAll_ShouldReturnListOfDtos() {
        when(eventCategoryService.getAll()).thenReturn(List.of(responseDto));
        List<EventCategoryResponseDto> result = eventCategoryController.getAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getIdEventCategory());
        assertEquals("Концерт", result.get(0).getEventCategoryName());
        assertEquals("#FF5733", result.get(0).getColorCode());
        verify(eventCategoryService, times(1)).getAll();
    }

    @Test
    void testGetAll_ShouldReturnEmptyList() {
        when(eventCategoryService.getAll()).thenReturn(List.of());
        List<EventCategoryResponseDto> result = eventCategoryController.getAll();
        assertTrue(result.isEmpty());
    }

    //для getById()
    @Test
    void testGetById_ShouldReturnDto_WhenExists() throws EntityNotFoundException {
        when(eventCategoryService.getById(1L)).thenReturn(responseDto);
        EventCategoryResponseDto result = eventCategoryController.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getIdEventCategory());
        assertEquals("Концерт", result.getEventCategoryName());
    }

    @Test
    void testGetById_ShouldThrowException_WhenNotExists() throws EntityNotFoundException {
        when(eventCategoryService.getById(999L))
                .thenThrow(new EntityNotFoundException("Категория с id 999 не найдена"));
        assertThrows(EntityNotFoundException.class, () -> eventCategoryController.getById(999L));
    }

    //для create()
    @Test
    void testCreate_ShouldReturnCreatedDto_WhenValid() throws DuplicateEntityException {
        when(eventCategoryService.create(any(EventCategoryRequestDto.class))).thenReturn(responseDto);
        EventCategoryResponseDto result = eventCategoryController.create(requestDto);
        assertNotNull(result);
        assertEquals(1L, result.getIdEventCategory());
        assertEquals("Концерт", result.getEventCategoryName());
        verify(eventCategoryService, times(1)).create(any(EventCategoryRequestDto.class));
    }

    @Test
    void testCreate_ShouldThrowException_WhenDuplicateName() throws DuplicateEntityException {
        when(eventCategoryService.create(any(EventCategoryRequestDto.class)))
                .thenThrow(new DuplicateEntityException("Категория с именем 'Концерт' уже существует"));
        assertThrows(DuplicateEntityException.class, () -> eventCategoryController.create(requestDto));
    }

    //для update()
    @Test
    void testUpdate_ShouldReturnUpdatedDto_WhenValid() throws DuplicateEntityException, EntityNotFoundException {
        EventCategoryRequestDto updateDto = new EventCategoryRequestDto();
        updateDto.setEventCategoryName("Рок-концерт");
        updateDto.setColorCode("#AA22FF");
        EventCategoryResponseDto updatedResponse = new EventCategoryResponseDto(1L, "Рок-концерт", "Обновлено", "#AA22FF");
        when(eventCategoryService.update(eq(1L), any(EventCategoryRequestDto.class))).thenReturn(updatedResponse);
        EventCategoryResponseDto result = eventCategoryController.update(1L, updateDto);
        assertNotNull(result);
        assertEquals("Рок-концерт", result.getEventCategoryName());
        assertEquals("#AA22FF", result.getColorCode());
        verify(eventCategoryService, times(1)).update(eq(1L), any(EventCategoryRequestDto.class));
    }

    @Test
    void testUpdate_ShouldThrowException_WhenEntityNotExists() throws DuplicateEntityException, EntityNotFoundException {
        when(eventCategoryService.update(eq(999L), any(EventCategoryRequestDto.class)))
                .thenThrow(new EntityNotFoundException("Категория с id 999 не найдена"));
        assertThrows(EntityNotFoundException.class, () -> eventCategoryController.update(999L, requestDto));
    }

    @Test
    void testUpdate_ShouldThrowException_WhenDuplicateName() throws DuplicateEntityException, EntityNotFoundException {
        when(eventCategoryService.update(eq(1L), any(EventCategoryRequestDto.class)))
                .thenThrow(new DuplicateEntityException("Категория с таким именем уже существует"));
        assertThrows(DuplicateEntityException.class, () -> eventCategoryController.update(1L, requestDto));
    }

    //для delete()
    @Test
    void testDelete_ShouldCallService_WhenExists() throws EntityNotFoundException {
        doNothing().when(eventCategoryService).delete(1L);
        assertDoesNotThrow(() -> eventCategoryController.delete(1L));
        verify(eventCategoryService, times(1)).delete(1L);
    }

    @Test
    void testDelete_ShouldThrowException_WhenNotExists() throws EntityNotFoundException {
        doThrow(new EntityNotFoundException("Категория с id 999 не найдена")).when(eventCategoryService).delete(999L);
        assertThrows(EntityNotFoundException.class, () -> eventCategoryController.delete(999L));
    }
}
