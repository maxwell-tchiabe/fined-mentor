package com.fined.mentor.auth.service;

import com.fined.mentor.auth.entity.Role;
import com.fined.mentor.auth.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.servlet.http.Cookie;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private User sampleUser;
    private final String jwtSecret = "1234567890123456789012345678901234567890"; 

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400L); // 1 day
        ReflectionTestUtils.setField(jwtService, "jwtCookie", "mentor-jwt");
        ReflectionTestUtils.setField(jwtService, "cookieSecure", false);
        ReflectionTestUtils.setField(jwtService, "cookieSameSite", "Lax");

        Role role = new Role();
        role.setName(Role.RoleName.ROLE_USER);

        sampleUser = User.builder()
                .id("1")
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of(role))
                .build();
    }

    @Test
    void generateToken_Success() {
        when(authentication.getPrincipal()).thenReturn(sampleUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> sampleUser.getAuthorities());

        String token = jwtService.generateToken(authentication);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("testuser", jwtService.getUsernameFromToken(token));
    }

    @Test
    void generateJwtCookie_Success() {
        when(authentication.getPrincipal()).thenReturn(sampleUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> sampleUser.getAuthorities());

        ResponseCookie cookie = jwtService.generateJwtCookie(authentication);

        assertNotNull(cookie);
        assertEquals("mentor-jwt", cookie.getName());
        assertNotNull(cookie.getValue());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void getCleanJwtCookie_Success() {
        ResponseCookie cookie = jwtService.getCleanJwtCookie();

        assertNotNull(cookie);
        assertEquals("mentor-jwt", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals("/api", cookie.getPath());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }

    @Test
    void getJwtFromCookies_Success() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("mentor-jwt", "sample-token-value"));

        String token = jwtService.getJwtFromCookies(request);

        assertEquals("sample-token-value", token);
    }

    @Test
    void getJwtFromCookies_NotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other-cookie", "other-value"));

        String token = jwtService.getJwtFromCookies(request);

        assertNull(token);
    }

    @Test
    void validateToken_InvalidSignature() {
        when(authentication.getPrincipal()).thenReturn(sampleUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> sampleUser.getAuthorities());
        String token = jwtService.generateToken(authentication);
        
        // Tamper with the token
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    void validateToken_MalformedToken() {
        assertFalse(jwtService.validateToken("this.is.not.a.valid.jwt"));
    }

    @Test
    void validateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 0L); // 0 seconds
        
        when(authentication.getPrincipal()).thenReturn(sampleUser);
        when(authentication.getAuthorities()).thenAnswer(invocation -> sampleUser.getAuthorities());
        String token = jwtService.generateToken(authentication);

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void validateToken_EmptyToken() {
        assertFalse(jwtService.validateToken(""));
    }
}
