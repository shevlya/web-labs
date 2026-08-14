package ru.ssau.srestapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ssau.srestapp.dto.email.ContactMessageRequestDto;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ContactControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void sendMessage_ValidRequest_ReturnsNoContent() throws Exception {
        ContactMessageRequestDto dto = new ContactMessageRequestDto();
        dto.setName("Дарья Шевлякова");
        dto.setEmail("shevliakova.d@gmail.com");
        dto.setSubject("Вопрос по мероприятию");
        dto.setMessage("Здравствуйте, хотел бы уточнить детали.");
        mockMvc.perform(post("/api/contact/send").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isNoContent());
    }

    @Test
    void sendMessage_InvalidEmail_ReturnsBadRequest() throws Exception {
        ContactMessageRequestDto dto = new ContactMessageRequestDto();
        dto.setName("Дарья Шевлякова");
        dto.setEmail("dasha");
        dto.setSubject("Вопрос");
        dto.setMessage("Не получается подать заявку, слишком высокий порог для минимального текста");
        mockMvc.perform(post("/api/contact/send").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
    }

    @Test
    void sendMessage_EmptyName_ReturnsBadRequest() throws Exception {
        ContactMessageRequestDto dto = new ContactMessageRequestDto();
        dto.setName("");
        dto.setEmail("daryas1890@gmail.com");
        dto.setSubject("Проблема с подачей заявки");
        dto.setMessage("Не получается подать заявку, слишком высокий порог для минимального текста");
        mockMvc.perform(post("/api/contact/send").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
    }

    @Test
    void sendMessage_MessageTooShort_ReturnsBadRequest() throws Exception {
        ContactMessageRequestDto dto = new ContactMessageRequestDto();
        dto.setName("Дарья Шевлякова");
        dto.setEmail("daryas1890@gmail.com");
        dto.setSubject("Проблема");
        dto.setMessage("коротко");
        mockMvc.perform(post("/api/contact/send").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
    }

    //чтобы не отправлять реальные письма
    @TestConfiguration
    static class TestMailConfig {
        @Bean
        @Primary
        public JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
    }
}
