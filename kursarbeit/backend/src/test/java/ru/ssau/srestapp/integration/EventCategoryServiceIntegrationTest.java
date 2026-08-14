package ru.ssau.srestapp.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryRequestDto;
import ru.ssau.srestapp.dto.eventCategory.EventCategoryResponseDto;
import ru.ssau.srestapp.entity.EventCategory;
import ru.ssau.srestapp.exception.DuplicateEntityException;
import ru.ssau.srestapp.exception.EntityNotFoundException;
import ru.ssau.srestapp.repository.EventCategoryRepository;
import ru.ssau.srestapp.service.EventCategoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventCategoryServiceIntegrationTest {

    @Autowired
    private EventCategoryService eventCategoryService;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    private EventCategory testCategory1;
    private EventCategory testCategory2;

    @BeforeEach
    void setUp() {
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

    //дял метода getAll()
    @Test
    void testGetAll_ShouldReturnAllCategories() {
        List<EventCategoryResponseDto> categories = eventCategoryService.getAll();
        assertEquals(2, categories.size());
        assertTrue(categories.stream().anyMatch(c -> "Концерт".equals(c.getEventCategoryName())));
        assertTrue(categories.stream().anyMatch(c -> "Выставка".equals(c.getEventCategoryName())));
    }

    //для метода getById()
    @Test
    void testGetById_ShouldReturnCorrectCategory() throws EntityNotFoundException {
        EventCategoryResponseDto found = eventCategoryService.getById(testCategory1.getIdEventCategory());
        assertEquals(testCategory1.getIdEventCategory(), found.getIdEventCategory());
        assertEquals("Концерт", found.getEventCategoryName());
        assertEquals("#FF5733", found.getColorCode());
    }

    @Test
    void testGetById_NotFound_ShouldThrowException() {
        Long nonExistentId = 999L;
        assertThrows(EntityNotFoundException.class, () -> eventCategoryService.getById(nonExistentId));
    }

    //для метода create() – успешное создание и дубликат
    @Test
    void testCreate_ShouldSaveNewCategory() throws DuplicateEntityException {
        EventCategoryRequestDto dto = new EventCategoryRequestDto();
        dto.setEventCategoryName("Спектакль");
        dto.setEventCategoryDescription("Театральные постановки");
        dto.setColorCode("#3357FF");
        EventCategoryResponseDto created = eventCategoryService.create(dto);
        assertNotNull(created.getIdEventCategory());
        assertEquals("Спектакль", created.getEventCategoryName());
        assertEquals(3, eventCategoryRepository.count());
    }

    @Test
    void testCreate_DuplicateName_ShouldThrowException() {
        EventCategoryRequestDto dto = new EventCategoryRequestDto();
        dto.setEventCategoryName("Концерт");
        dto.setEventCategoryDescription("Любой");
        dto.setColorCode("#000000");
        assertThrows(DuplicateEntityException.class, () -> eventCategoryService.create(dto));
    }

    //для метода update()
    @Test
    void testUpdate_ShouldModifyExistingCategory() throws Exception {
        EventCategoryRequestDto updateDto = new EventCategoryRequestDto();
        updateDto.setEventCategoryName("Рок-концерт");
        updateDto.setEventCategoryDescription("Рок музыка");
        updateDto.setColorCode("#AA22FF");
        EventCategoryResponseDto updated = eventCategoryService.update(testCategory1.getIdEventCategory(), updateDto);
        assertEquals(testCategory1.getIdEventCategory(), updated.getIdEventCategory());
        assertEquals("Рок-концерт", updated.getEventCategoryName());
        assertEquals("#AA22FF", updated.getColorCode());
        EventCategory entity = eventCategoryRepository.findById(testCategory1.getIdEventCategory()).orElseThrow();
        assertEquals("Рок-концерт", entity.getEventCategoryName());
    }

    @Test
    void testUpdate_ToExistingName_ShouldThrowException() {
        EventCategoryRequestDto updateDto = new EventCategoryRequestDto();
        updateDto.setEventCategoryName("Выставка");
        updateDto.setEventCategoryDescription("Новое описание");
        updateDto.setColorCode("#FFFFFF");

        assertThrows(DuplicateEntityException.class,
                () -> eventCategoryService.update(testCategory1.getIdEventCategory(), updateDto));
    }

    //для метода delete()
    @Test
    void testDelete_ShouldRemoveCategory() throws EntityNotFoundException {
        Long idToDelete = testCategory1.getIdEventCategory();
        eventCategoryService.delete(idToDelete);

        assertFalse(eventCategoryRepository.existsById(idToDelete));
        assertEquals(1, eventCategoryRepository.count());
    }

    @Test
    void testDelete_NotFound_ShouldThrowException() {
        assertThrows(EntityNotFoundException.class, () -> eventCategoryService.delete(999L));
    }
}
