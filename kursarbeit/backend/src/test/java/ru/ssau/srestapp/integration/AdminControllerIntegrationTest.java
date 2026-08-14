package ru.ssau.srestapp.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ssau.srestapp.controller.AdminController;
import ru.ssau.srestapp.dto.admin.AdminStatisticsDto;
import ru.ssau.srestapp.entity.*;
import ru.ssau.srestapp.repository.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private AdminController adminController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private OrganizerRequestRepository organizerRequestRepository;

    @BeforeEach
    void before() {
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "USER", null)));
        Role organizerRole = roleRepository.findByRoleName("ORGANIZER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ORGANIZER", null)));

        User activeUser = new User();
        activeUser.setUserStatus(UserStatus.ACTIVE);
        activeUser.setRole(userRole);
        activeUser.setFio("Дарья Шевлякова");
        activeUser.setEmail("shevliakova.d@gmail.com");
        activeUser.setPasswordHash("1234567");
        activeUser.setHasDisability(false);
        activeUser = userRepository.save(activeUser);

        User organizerUser = new User();
        organizerUser.setUserStatus(UserStatus.ACTIVE);
        organizerUser.setRole(organizerRole);
        organizerUser.setFio("Арина Шапорева");
        organizerUser.setEmail("deutsch.kedg@gmail.com");
        organizerUser.setPasswordHash("1234567");
        organizerUser.setHasDisability(false);
        organizerUser = userRepository.save(organizerUser);

        EventCategory category = new EventCategory();
        category.setEventCategoryName("Концерт");
        category.setEventCategoryDescription("Живая музыка");
        category.setColorCode("#FF5733");
        category = eventCategoryRepository.save(category);

        Event pendingEvent = new Event();
        pendingEvent.setOrganizer(organizerUser);
        pendingEvent.setEventFormat(EventFormat.OFFLINE);
        pendingEvent.setEventStatus(EventStatus.PLANNED);
        pendingEvent.setEventCategory(category);
        pendingEvent.setEventName("Рок-концерт");
        pendingEvent.setEventDate(LocalDateTime.now().plusDays(10));
        pendingEvent.setStartTime(LocalDateTime.now().plusDays(10).withHour(19));
        pendingEvent.setEndTime(LocalDateTime.now().plusDays(10).withHour(22));
        pendingEvent.setMaxParticipants(500);
        pendingEvent.setVerified(false);
        eventRepository.save(pendingEvent);

        OrganizerRequest pendingRequest = new OrganizerRequest();
        pendingRequest.setUser(activeUser);
        pendingRequest.setRequestStatus(RequestStatus.PENDING);
        pendingRequest.setRequestText("Хочу стать организатором");
        pendingRequest.setSubmittedAt(LocalDateTime.now());
        organizerRequestRepository.save(pendingRequest);
    }

    @AfterEach
    void after() {
        organizerRequestRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        eventCategoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
    }

    @Test
    void testGetStatistics() {
        AdminStatisticsDto stats = adminController.getStatistics();
        assertEquals(2, stats.getTotalUsers());
        assertEquals(1, stats.getTotalOrganizers());
        assertEquals(1, stats.getPendingEvents());
        assertEquals(1, stats.getPendingOrganizerRequests());
    }
}