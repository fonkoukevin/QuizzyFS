package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.entity.User;
import com.quizzy.quizzy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class) // ✅ Use @Import for mock beans
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService; // ✅ No more @MockBean

    private final String TEST_UID = "test-user-123";
    private final String TEST_EMAIL = "test@example.com";

    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class); // ✅ Manually create a mock bean
        }
    }

    @BeforeEach
    void setup() {
        // Mock user data
        User user = new User();
        user.setUid(TEST_UID);
        user.setUsername("test-user");

        when(userService.findUserById(TEST_UID)).thenReturn(Optional.of(user));
    }

    @Test
    @WithMockUser(username = "test-user-123", roles = "USER")
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value(TEST_UID))
                .andExpect(jsonPath("$.username").value("test-user"));
    }
}
