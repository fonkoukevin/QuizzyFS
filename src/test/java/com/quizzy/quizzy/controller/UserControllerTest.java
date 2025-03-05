package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import utilsTest.MockMvcTestHelper;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class) // Active Mockito
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Helper custom de test
    private MockMvcTestHelper mockMvcHelper;

    @BeforeEach
    void setUp() {
        mockMvcHelper = new MockMvcTestHelper(mockMvc);
    }

    @Test
    void testCreatedUser() throws Exception {

        String username = "TheUser";
        var userDTO = new UserRequestDTO();
        userDTO.setUsername(username);

        var createRequest = mockMvcHelper.post("/api/users", userDTO);
        assertEquals(201, createRequest.status());

        var getRequest = mockMvcHelper.get("/api/users/me",  UserDto.class);
        UserDto getUserDTO = getRequest.body();
        assertEquals(username, getUserDTO.username());
        assertEquals(200, getRequest.status());
    }
}
