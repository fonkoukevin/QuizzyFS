package com.quizzy.quizzy.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

@WebMvcTest(PingController.class) // Test du contrôleur uniquement
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate; // Simule l'accès à la base de données

    @Test
    void testPing_DatabaseOK() throws Exception {
        // Simule une requête SQL réussie
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);

        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk()) // Vérifie le code 200
                .andExpect(jsonPath("$.status").value("OK")) // Vérifie le statut dans la réponse JSON
                .andExpect(jsonPath("$.details.database").value("OK")); // Vérifie que la DB est OK
    }

    @Test
    void testPing_DatabaseKO() throws Exception {
        // Simule une exception quand la DB ne répond pas
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenThrow(new RuntimeException("DB Down"));

        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isInternalServerError()) // Vérifie le code 500
                .andExpect(jsonPath("$.status").value("Partial")) // Vérifie le statut JSON
                .andExpect(jsonPath("$.details.database").value("KO")); // Vérifie que la DB est KO
    }
}
