package ru.ssau.srestapp.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.srestapp.exception.UtilException;
import ru.ssau.srestapp.util.JsonConverter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JsonConverterTest {

    private final JsonConverter converter = new JsonConverter();

    @Test
    void convertToDatabaseColumn_WithValidMap_ShouldReturnJsonString() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Концерт");
        map.put("maxParticipants", 100);
        map.put("price", 255);
        String result = converter.convertToDatabaseColumn(map);
        assertNotNull(result);
        assertTrue(result.contains("\"name\":\"Концерт\""));
        assertTrue(result.contains("\"maxParticipants\":100"));
        assertTrue(result.contains("\"price\":255"));
    }

    @Test
    void convertToDatabaseColumn_WithNull_ShouldReturnNull() {
        String result = converter.convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_WithValidJson_ShouldReturnMap() {
        String json = "{\"name\":\"Концерт\",\"maxParticipants\":100,\"price\":255}";
        Map<String, Object> result = converter.convertToEntityAttribute(json);
        assertNotNull(result);
        assertEquals("Концерт", result.get("name"));
        assertEquals(100, result.get("maxParticipants"));
        assertEquals(255, result.get("price"));
    }

    @Test
    void convertToEntityAttribute_WithNull_ShouldReturnNull() {
        Map<String, Object> result = converter.convertToEntityAttribute(null);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_WithEmptyString_ShouldReturnNull() {
        Map<String, Object> result = converter.convertToEntityAttribute("");
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_WithBlankString_ShouldReturnNull() {
        Map<String, Object> result = converter.convertToEntityAttribute("   ");
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_WithInvalidJson_ShouldThrowUtilException() {
        String invalidJson = "{\"name\":\"Концерт\"";
        UtilException exception = assertThrows(UtilException.class, () -> converter.convertToEntityAttribute(invalidJson));
        assertEquals("Не удалось преобразовать JSON в Map", exception.getMessage());
        assertNotNull(exception.getCause());
        assertInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class, exception.getCause());
    }

    @Test
    void convertToDatabaseColumnAndBack_ShouldBeConsistent() {
        Map<String, Object> original = new HashMap<>();
        original.put("key1", "value1");
        original.put("key2", 123);
        original.put("key3", true);
        String json = converter.convertToDatabaseColumn(original);
        Map<String, Object> restored = converter.convertToEntityAttribute(json);
        assertEquals(original, restored);
    }
}