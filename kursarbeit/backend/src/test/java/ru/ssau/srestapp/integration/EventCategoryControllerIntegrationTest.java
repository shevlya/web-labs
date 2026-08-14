package ru.ssau.srestapp.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ssau.srestapp.controller.EventCategoryController;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryRequestDto;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryResponseDto;
import ru.ssau.srestapp.entity.EventCategory;
import ru.ssau.srestapp.exception.DuplicateEntityException;
import ru.ssau.srestapp.exception.EntityNotFoundException;
import ru.ssau.srestapp.repository.EventCategoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EventCategoryControllerIntegrationTest {

    @Autowired
    private EventCategoryController eventCategoryController;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    private EventCategory testCategory1;
    private EventCategory testCategory2;

    @BeforeEach
    void setUp() {
        eventCategoryRepository.deleteAllInBatch();
        testCategory1 = new EventCategory();
        testCategory1.setEventCategoryName("Концерт");
        testCategory1.setEventCategoryDescription("Живая музыка");
        testCategory1.setColorCode("#FF5733");
        testCategory1 = eventCategoryRepository.save(testCategory1);

        testCategory2 = new EventCategory();
        testCategory2.setEventCategoryName("Выставка");
        testCategory2.setEventCategoryDescription("Искусство и живопись");
        testCategory2.setColorCode("#33FF57");
        testCategory2 = eventCategoryRepository.save(testCategory2);
    }

    @AfterEach
    void tearDown() {
        eventCategoryRepository.deleteAllInBatch();
    }

    //для метода getAll()
    @Test
    void testGetAll_ShouldReturnAllCategories() {
        List<EventCategoryResponseDto> result = eventCategoryController.getAll();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> "Концерт".equals(c.getEventCategoryName())));
        assertTrue(result.stream().anyMatch(c -> "Выставка".equals(c.getEventCategoryName())));
    }

    //для метода getById()
    @Test
    void testGetById_ShouldReturnCorrectCategory() throws EntityNotFoundException {
        EventCategoryResponseDto result = eventCategoryController.getById(testCategory1.getIdEventCategory());
        assertNotNull(result);
        assertEquals(testCategory1.getIdEventCategory(), result.getIdEventCategory());
        assertEquals("Концерт", result.getEventCategoryName());
        assertEquals("#FF5733", result.getColorCode());
    }

    @Test
    void testGetById_NotFound_ShouldThrowException() {
        assertThrows(EntityNotFoundException.class, () -> eventCategoryController.getById(999L));
    }

    //для метода create()
    @Test
    void testCreate_ShouldSaveNewCategory() throws DuplicateEntityException {
        EventCategoryRequestDto dto = new EventCategoryRequestDto();
        dto.setEventCategoryName("Спектакль");
        dto.setEventCategoryDescription("Театральные постановки");
        dto.setColorCode("#3357FF");
        EventCategoryResponseDto result = eventCategoryController.create(dto);
        assertNotNull(result.getIdEventCategory());
        assertEquals("Спектакль", result.getEventCategoryName());
        assertEquals("#3357FF", result.getColorCode());
        assertTrue(eventCategoryRepository.existsByEventCategoryName("Спектакль"));
    }

    @Test
    void testCreate_DuplicateName_ShouldThrowException() {
        EventCategoryRequestDto dto = new EventCategoryRequestDto();
        dto.setEventCategoryName("Концерт");
        dto.setEventCategoryDescription("Дубликат");
        dto.setColorCode("#000000");
        assertThrows(DuplicateEntityException.class, () -> eventCategoryController.create(dto));
    }

    //для метода update()
    @Test
    void testUpdate_ShouldModifyExistingCategory() throws Exception {
        EventCategoryRequestDto updateDto = new EventCategoryRequestDto();
        updateDto.setEventCategoryName("Рок-концерт");
        updateDto.setEventCategoryDescription("Рок музыка");
        updateDto.setColorCode("#AA22FF");
        EventCategoryResponseDto result = eventCategoryController.update(testCategory1.getIdEventCategory(), updateDto);
        assertEquals(testCategory1.getIdEventCategory(), result.getIdEventCategory());
        assertEquals("Рок-концерт", result.getEventCategoryName());
        assertEquals("#AA22FF", result.getColorCode());
        EventCategory updated = eventCategoryRepository.findById(testCategory1.getIdEventCategory()).orElseThrow();
        assertEquals("Рок-концерт", updated.getEventCategoryName());
    }

    @Test
    void testUpdate_ToExistingName_ShouldThrowException() {
        EventCategoryRequestDto updateDto = new EventCategoryRequestDto();
        updateDto.setEventCategoryName("Выставка");
        updateDto.setEventCategoryDescription("Новое описание");
        updateDto.setColorCode("#FFFFFF");
        assertThrows(DuplicateEntityException.class, () -> eventCategoryController.update(testCategory1.getIdEventCategory(), updateDto));
    }

    //для метода delete()
    @Test
    void testDelete_ShouldRemoveCategory() throws EntityNotFoundException {
        Long idToDelete = testCategory1.getIdEventCategory();
        eventCategoryController.delete(idToDelete);
        assertFalse(eventCategoryRepository.existsById(idToDelete));
        assertEquals(1, eventCategoryRepository.count());
    }

    @Test
    void testDelete_NotFound_ShouldThrowException() {
        assertThrows(EntityNotFoundException.class, () -> eventCategoryController.delete(999L));
    }
}
