package com.quizzy.quizzy.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import utilsTest.JwtValidator;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    //@Autowired
    private MockMvc mockMvc;

    public JwtValidator jwtValidator;

    private UserControllerTest(@Autowired  MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.jwtValidator = new JwtValidator(mockMvc);
    }

    @Test
    void testUserController() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content("{\"username\": \"test\"}")
                .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().isCreated());

//        mockMvc.perform(get("/api/users/me")
//                .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser").claim("email", "test@email.com"))))
//                .andExpect(status().isOk());

         jwtValidator.performValidation("/api/users/me");

    }
}
