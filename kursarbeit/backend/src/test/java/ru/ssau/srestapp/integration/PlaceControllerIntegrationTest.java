package ru.ssau.srestapp.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.srestapp.controller.PlaceController;
import ru.ssau.srestapp.dto.place.PlaceResponseDto;
import ru.ssau.srestapp.entity.OnlinePlace;
import ru.ssau.srestapp.entity.PhysicalPlace;
import ru.ssau.srestapp.repository.PlaceRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceControllerIntegrationTest {

    @Autowired
    private PlaceController placeController;

    @Autowired
    private PlaceRepository placeRepository;

    private Long physicalPlaceId;
    private Long onlinePlaceId;

    @BeforeEach
    void before() {
        PhysicalPlace physicalPlace = new PhysicalPlace();
        physicalPlace.setPlaceName("Корпус 14");
        physicalPlace.setPlaceDescription("423 аудитория");
        physicalPlace.setAddress("ул. Гая, 14");
        physicalPlace.setDisabilityAccessible(true);
        physicalPlace = placeRepository.save(physicalPlace);
        physicalPlaceId = physicalPlace.getIdPlace();

        OnlinePlace onlinePlace = new OnlinePlace();
        onlinePlace.setPlaceName("BBB-конференция");
        onlinePlace.setPlaceDescription("Онлайн встреча");
        onlinePlace.setMeetingUrl("https://bbb.ssau.ru/b/cry-hlj-c69-cor");
        onlinePlace.setRecording(true);
        onlinePlace = placeRepository.save(onlinePlace);
        onlinePlaceId = onlinePlace.getIdPlace();
    }

    @Test
    void testGetAll_ShouldReturnAllPlaces() {
        List<PlaceResponseDto> places = placeController.getAll();
        assertEquals(2, places.size());
        PlaceResponseDto physical = places.stream()
                .filter(p -> p.getIdPlace().equals(physicalPlaceId))
                .findFirst()
                .orElseThrow();
        assertEquals("Корпус 14", physical.getPlaceName());
        assertEquals("PHYSICAL", physical.getType());
        PlaceResponseDto online = places.stream()
                .filter(p -> p.getIdPlace().equals(onlinePlaceId))
                .findFirst()
                .orElseThrow();
        assertEquals("BBB-конференция", online.getPlaceName());
        assertEquals("ONLINE", online.getType());
    }
}