package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.dto.AllQuizUserDTO;
import com.quizzy.quizzy.dto.QuizDTO;
import com.quizzy.quizzy.dto.QuizUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import utilsTest.MockMvcTestHelper;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class) // Active Mockito
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockMvcTestHelper mockMvcHelper;

    @BeforeEach
    void setUp() {
        mockMvcHelper = new MockMvcTestHelper(mockMvc);
    }

    @Test
    void testCreatedQuiz() throws Exception {
        var quizDto = new QuizDTO();
        quizDto.setTitle("My quiz");
        quizDto.setDescription("Description");
        var createRequest = mockMvcHelper.post("/api/quiz", quizDto);
        assertEquals(201, createRequest.status());

        var quizId = createRequest.getLocationId();

        var getRequest = mockMvcHelper.get("/api/quiz",  AllQuizUserDTO.class);
        AllQuizUserDTO allQuiz = getRequest.body();
        Optional<QuizUserDTO> oneQuiz = allQuiz.data().stream().filter(q -> q.id().equals(quizId)).findFirst();;
        assertTrue(oneQuiz.isPresent());
        // TODO check quizz is here and has the right details.
    }

    // Test Issue 7
//    @Test
//    void testGetQuiz() throws Exception {
//        // Récupération du body avec tous les Quiz
//        var getRequest = mockMvcHelper.get("/api/quiz", AllQuizUserDTO.class);
//        AllQuizUserDTO allQuiz = getRequest.body();
//
//        var quizDto = new QuizDTO();
//        QuizDTO lastQuizDto = allQuiz.data().stream().filter( q -> )
//
//
//    }
}
