package com.quizzy.quizzy.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class) // Active Mockito
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPing_DatabaseOK() throws Exception {

        try {
            // Simule une base qui répond bien
            mockMvc.perform(get("/api/ping")
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                            .andExpect(status().isOk()) // Vérifie HTTP 200
                            .andExpect(jsonPath("$.status").value("OK"))
                            .andExpect(jsonPath("$.details.database").value("OK"));

            var response = mockMvc.perform(get("/api/ping")
                    .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))
                    )
            ).andReturn().getResponse();
            System.out.println("Test");
        } catch(Exception e) {
            throw e;
        }


    }
}
