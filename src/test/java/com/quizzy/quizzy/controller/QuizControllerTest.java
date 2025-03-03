package com.quizzy.quizzy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizzy.quizzy.dto.QuizDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import utilsTest.MockMvcTestHelper;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class) // Active Mockito
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockMvcTestHelper mockMvcHelper = new MockMvcTestHelper();

    @Test
    @WithMockUser
    void testQuizController() throws Exception {
        mockMvc.perform(get("/api/quiz")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().is4xxClientError());
        /*var body = mockMvcHelper.performRequest(mockMvc, "/api/quiz", "GET", "{}", 200);
        System.out.println("Return  body 3 " + body);*/
    }

    @Test
    void testCreateQuizController() throws Exception {

//        HashMap<String, String> map = new HashMap<>();
//        map.put("description", "Ma description");
//        map.put("title", "Mon titre");

        QuizDTO dtoToCompare = new QuizDTO();
        dtoToCompare.setDescription("Ma description");
        dtoToCompare.setTitle("Mon titre");

//        ObjectMapper objectMapper = new ObjectMapper();
//        String json = objectMapper.writeValueAsString(map);

        //System.out.println(dtoToCompare);

        var body = mockMvcHelper.performRequest(mockMvc, "/api/quiz", "POST", String.valueOf(dtoToCompare), 201);
        System.out.println("Return  body 1 " + body);
    }
}
