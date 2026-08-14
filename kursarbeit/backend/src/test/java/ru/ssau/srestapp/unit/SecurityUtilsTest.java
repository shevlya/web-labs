package ru.ssau.srestapp.unit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.ssau.srestapp.exception.UtilException;
import ru.ssau.srestapp.security.CustomUserDetails;
import ru.ssau.srestapp.util.SecurityUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserDetails_WithAuthenticatedUser_ShouldReturnUserDetails() {
        CustomUserDetails userDetails = new CustomUserDetails(1L,
                "shevliakova.d@gmail.com",
                "123456",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails result = SecurityUtils.getCurrentUserDetails();
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("shevliakova.d@gmail.com", result.getEmail());
    }

    @Test
    void getCurrentUserId_ShouldReturnUserId() {
        CustomUserDetails userDetails = new CustomUserDetails(42L,
                "shevlyak@mail.ru",
                "1234567",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Long userId = SecurityUtils.getCurrentUserId();
        assertEquals(42L, userId);
    }

    @Test
    void getCurrentUserDetails_WhenNoAuthentication_ShouldThrowUtilException() {
        UtilException exception = assertThrows(UtilException.class, SecurityUtils::getCurrentUserDetails);
        assertEquals("Пользователь не аутентифицирован", exception.getMessage());
    }

    @Test
    void getCurrentUserDetails_WhenAuthenticationIsNull_ShouldThrowUtilException() {
        SecurityContextHolder.getContext().setAuthentication(null);
        UtilException exception = assertThrows(UtilException.class, SecurityUtils::getCurrentUserDetails);
        assertEquals("Пользователь не аутентифицирован", exception.getMessage());
    }

    @Test
    void getCurrentUserDetails_WhenNotAuthenticated_ShouldThrowUtilException() {
        Authentication notAuthenticated = new UsernamePasswordAuthenticationToken("user", "pass");
        notAuthenticated.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(notAuthenticated);
        UtilException exception = assertThrows(UtilException.class, SecurityUtils::getCurrentUserDetails);
        assertEquals("Пользователь не аутентифицирован", exception.getMessage());
    }
}
