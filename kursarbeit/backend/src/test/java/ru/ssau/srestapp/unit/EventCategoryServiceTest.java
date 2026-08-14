package ru.ssau.srestapp.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryRequestDto;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryResponseDto;
import ru.ssau.srestapp.entity.EventCategory;
import ru.ssau.srestapp.exception.DuplicateEntityException;
import ru.ssau.srestapp.exception.EntityNotFoundException;
import ru.ssau.srestapp.repository.EventCategoryRepository;
import ru.ssau.srestapp.service.EventCategoryService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCategoryServiceTest {

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @InjectMocks
    private EventCategoryService eventCategoryService;

    private EventCategory testEntity;
    private EventCategoryRequestDto requestDto;
    private EventCategoryResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testEntity = new EventCategory();
        testEntity.setIdEventCategory(1L);
        testEntity.setEventCategoryName("Концерт");
        testEntity.setEventCategoryDescription("Живая музыка");
        testEntity.setColorCode("#FF5733");

        requestDto = new EventCategoryRequestDto();
        requestDto.setEventCategoryName("Концерт");
        requestDto.setEventCategoryDescription("Живая музыка");
        requestDto.setColorCode("#FF5733");

        responseDto = new EventCategoryResponseDto(1L, "Концерт", "Живая музыка", "#FF5733");
    }

    //для getAll()
    @Test
    void testGetAll_ShouldReturnListOfDtos() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of(testEntity));
        List<EventCategoryResponseDto> result = eventCategoryService.getAll();
        assertEquals(1, result.size());
        assertEquals(responseDto.getIdEventCategory(), result.get(0).getIdEventCategory());
        assertEquals(responseDto.getEventCategoryName(), result.get(0).getEventCategoryName());
        verify(eventCategoryRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_ShouldReturnEmptyList_WhenNoData() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of());
        List<EventCategoryResponseDto> result = eventCategoryService.getAll();
        assertTrue(result.isEmpty());
    }

    //для getById()
    @Test
    void testGetById_ShouldReturnDto_WhenEntityExists() throws EntityNotFoundException {
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        EventCategoryResponseDto result = eventCategoryService.getById(1L);
        assertNotNull(result);
        assertEquals(responseDto.getIdEventCategory(), result.getIdEventCategory());
        assertEquals(responseDto.getEventCategoryName(), result.getEventCategoryName());
    }

    @Test
    void testGetById_ShouldThrowException_WhenEntityNotFound() {
        when(eventCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventCategoryService.getById(999L));
    }

    //для create()
    @Test
    void testCreate_ShouldSaveAndReturnDto_WhenNameIsUnique() throws DuplicateEntityException {
        when(eventCategoryRepository.existsByEventCategoryName("Концерт")).thenReturn(false);
        when(eventCategoryRepository.save(any(EventCategory.class))).thenReturn(testEntity);
        EventCategoryResponseDto result = eventCategoryService.create(requestDto);
        assertNotNull(result);
        assertEquals(responseDto.getEventCategoryName(), result.getEventCategoryName());
        verify(eventCategoryRepository, times(1)).save(any(EventCategory.class));
    }

    @Test
    void testCreate_ShouldThrowException_WhenNameAlreadyExists() {
        when(eventCategoryRepository.existsByEventCategoryName("Концерт")).thenReturn(true);
        assertThrows(DuplicateEntityException.class, () -> eventCategoryService.create(requestDto));
        verify(eventCategoryRepository, never()).save(any(EventCategory.class));
    }

    //для update()
    @Test
    void testUpdate_ShouldModifyAndReturnDto_WhenEntityExistsAndNameIsUnique() throws DuplicateEntityException, EntityNotFoundException {
        EventCategoryRequestDto updateDto = new EventCategoryRequestDto();
        updateDto.setEventCategoryName("Рок-концерт");
        updateDto.setEventCategoryDescription("Обновлённое описание");
        updateDto.setColorCode("#AA22FF");
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(eventCategoryRepository.existsByEventCategoryName("Рок-концерт")).thenReturn(false);
        when(eventCategoryRepository.save(any(EventCategory.class))).thenReturn(testEntity);
        EventCategoryResponseDto result = eventCategoryService.update(1L, updateDto);
        assertNotNull(result);
        assertEquals("Рок-концерт", result.getEventCategoryName());
        verify(eventCategoryRepository, times(1)).save(any(EventCategory.class));
    }

    @Test
    void testUpdate_ShouldThrowException_WhenEntityNotFound() {
        when(eventCategoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> eventCategoryService.update(999L, requestDto));
    }

    @Test
    void testUpdate_ShouldThrowException_WhenNewNameAlreadyExists() {
        EventCategoryRequestDto dtoWithNewName = new EventCategoryRequestDto();
        dtoWithNewName.setEventCategoryName("Другое название");
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(eventCategoryRepository.existsByEventCategoryName("Другое название")).thenReturn(true);
        assertThrows(DuplicateEntityException.class, () -> eventCategoryService.update(1L, dtoWithNewName));
    }

    //для delete()
    @Test
    void testDelete_ShouldRemoveEntity_WhenItExists() throws EntityNotFoundException {
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        doNothing().when(eventCategoryRepository).deleteById(1L);
        assertDoesNotThrow(() -> eventCategoryService.delete(1L));
        verify(eventCategoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_ShouldThrowException_WhenEntityNotFound() {
        when(eventCategoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> eventCategoryService.delete(999L));
        verify(eventCategoryRepository, never()).deleteById(anyLong());
    }
}
