package com.calendar.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void constructor_CreatesUserWithRequiredFields() {
        User user = new User("testuser", "test@example.com", "password123");
        
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        User user = new User();
        
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("secret");
        
        assertEquals(1L, user.getId());
        assertEquals("john", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
    }

    @Test
    void userDetails_AccountNonExpired_ReturnsTrue() {
        User user = new User("testuser", "test@example.com", "password");
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void userDetails_AccountNonLocked_ReturnsTrue() {
        User user = new User("testuser", "test@example.com", "password");
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void userDetails_CredentialsNonExpired_ReturnsTrue() {
        User user = new User("testuser", "test@example.com", "password");
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void userDetails_Enabled_ReturnsTrue() {
        User user = new User("testuser", "test@example.com", "password");
        assertTrue(user.isEnabled());
    }

    @Test
    void userDetails_Authorities_ReturnsEmptyList() {
        User user = new User("testuser", "test@example.com", "password");
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void createdAt_IsSetAutomatically() {
        User user = new User("testuser", "test@example.com", "password");
        assertNotNull(user.getCreatedAt());
    }
}
