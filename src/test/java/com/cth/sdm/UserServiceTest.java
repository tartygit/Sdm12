package com.cth.sdm;

import com.cth.sdm.model.SdmUser;
import com.cth.sdm.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testUserCreationAndStrengthValidation() {
        // Assert weak password exceptions
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("user1", "weak", "user1@cth.com", "User One", "MAKER");
        });

        // Assert valid password accepts
        SdmUser saved = userService.createUser("user2", "StrongPass@2026", "user2@cth.com", "User Two", "MAKER");
        Assertions.assertNotNull(saved);
        Assertions.assertEquals("user2", saved.getUsername());
        Assertions.assertFalse(saved.isLocked());
    }

    @Test
    public void testUserLockUnlock() {
        userService.createUser("user3", "StrongPass@2026", "user3@cth.com", "User Three", "MAKER");

        // Lock user
        userService.lockUser("user3");
        ListUsersContainsLocked("user3", true);

        // Unlock user
        userService.unlockUser("user3");
        ListUsersContainsLocked("user3", false);
    }

    private void ListUsersContainsLocked(String username, boolean expectedLocked) {
        SdmUser user = userService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(user);
        Assertions.assertEquals(expectedLocked, user.isLocked());
    }
}
