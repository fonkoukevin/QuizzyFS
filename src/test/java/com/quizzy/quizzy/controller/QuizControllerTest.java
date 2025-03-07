package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.dto.*;
import com.quizzy.quizzy.entity.Question;
import com.quizzy.quizzy.repository.QuestionRepository;
import com.quizzy.quizzy.repository.QuizRepository;
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

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
        var quizDto = new QuizDTO("My quiz", "My Description"); // ✅ Passer les valeurs directement

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
    /* Goal : Enable to retrieve a quiz by its id. Only works if user owns the quiz
    Specification:
        Endpoint: GET /api/quiz/<id>
        Input: none
        Output:
        Status: 200 if quiz is found
        Body: { title: xxx, description: xxx, questions: [<list of questions, see definition later>]}
        Error cases: 404 if quiz does not exist or does not belong to owner
     */
    @Test
    void testGetQuiz() throws Exception {

        // Récupération du body avec tous les Quiz
        var getRequest = mockMvcHelper.get("/api/quiz", AllQuizUserDTO.class);
        AllQuizUserDTO allQuiz = getRequest.body();

        List<QuizUserDTO> quizUserDTOList = allQuiz.data();
        QuizUserDTO firstQuizUser = quizUserDTOList.get(0);

        // Apple de la route d'un seul quiz
        var getQuizById = mockMvcHelper.get("/api/quiz/" + firstQuizUser.id(),  QuizDetailsDTO.class);
        QuizDetailsDTO quizDetails = getQuizById.body();

        // Verification cohérence des données
        assertEquals(firstQuizUser.title(), quizDetails.title());
        assertEquals(firstQuizUser.description(), quizDetails.description());
        assertEquals(200, getQuizById.status());

        var errorFakeUser = mockMvcHelper.get("/api/quiz/bad-id-user",  QuizDetailsDTO.class);
        assertEquals(404, errorFakeUser.status());

    }

    /* // Test Issue 8
    Endpoint: PATCH /api/quiz/<id>
    Input: [{op: "replace", path: "/title", value: "<new title>"}]
    Output:
    Status: 204 (No content)
    Body: <none>
    Error: 404 if quiz does not exist or quiz does not belong to user
    */
    @Test
    void testUpdateQuiz() throws Exception {
        String quizId = this.getFirstIdQuiz().id();

        // Création du Payload
        List<Map< String, String>> payload = new ArrayList<>();
        Map<String, String> operation = new HashMap<>();
        operation.put("op", "replace");
        operation.put("path", "/title");
        operation.put("value", "New title Test");
        payload.add(operation); // Mise de la map dan la liste

        var createRequest = mockMvcHelper.patch("/api/quiz/" + quizId, payload);
        assertEquals(204, createRequest.status());

        var errorRequest = mockMvcHelper.patch("/api/quiz/id-doesnt-exist", payload);
        assertEquals(404, errorRequest.status());
    }

    /* Issue 9
    Goal : Add a new question to the quiz. The question might be valid (i.e. already contain answers) or not.
    Specification:
        Endpoint: POST /api/quiz/<id>/questions
        Input: { title: 'the question', answers: [{'title': 'answer1', isCorrect: true}, {title: 'answer2', isCorrect: false}] }
        Output:
        Status: 201 + Header Location
        Body: <none>
     */
    @Test
    void testAddQuiz() throws Exception {
        String quizId = this.getFirstIdQuiz().id();
        String quizTitle = this.getFirstIdQuiz().title();

        // Création du Payload
        List<AnswerDTO> answerList = new ArrayList<>();
        AnswerDTO answer1 = new AnswerDTO("Ma réponse 1", true);
        AnswerDTO answer2 = new AnswerDTO("Ma réponse 2", false);
        AnswerDTO answer3 = new AnswerDTO("MA réponse 3", false);

        answerList.add(answer1);
        answerList.add(answer2);
        answerList.add(answer3);

        QuestionDTO payload = new QuestionDTO(quizId, quizTitle, answerList);

        // Création de la requete MockMVC
        var createRequest = mockMvcHelper.post("/api/quiz/" + quizId + "/questions", payload);
        assertNotNull(createRequest.headers().get("Location"));
        assertEquals(201, createRequest.status());

    }

    /* Issue 10
    Goal : Enhance the quiz details endpoint with the list of questions (if not already done)
    Specification:
        Endpoint: GET /api/quiz/<id>
        Input: <none>
        Output:
        Status: 200
        Body: {title: 'Quiz title', questions: [{id: '1234', title: 'question1', answers: [{ title: 'answer1', isCorrect: false}, ...] }] }
        Error codes : same as story Get quiz by id #7
     */
    @Test
    void testDetailsQuiz() throws Exception {

        // Récupération du body avec tous les Quiz
        var getRequest = mockMvcHelper.get("/api/quiz", AllQuizUserDTO.class);
        AllQuizUserDTO allQuiz = getRequest.body();

        List<QuizUserDTO> quizUserDTOList = allQuiz.data();
        QuizUserDTO firstQuizUser = quizUserDTOList.get(0);

        // Apple de la route d'un seul quiz
        var getQuizById = mockMvcHelper.get("/api/quiz/" + firstQuizUser.id(),  QuizDetailsDTO.class);
        QuizDetailsDTO quizDetails = getQuizById.body();

        System.out.println(quizDetails);

        // Verification cohérence des données
        assertEquals(firstQuizUser.title(), quizDetails.title());
        assertEquals(firstQuizUser.description(), quizDetails.description());
        assertEquals(200, getQuizById.status());

        // Test en cas de mauvais userID ou userId non présent dans la base
        var errorFakeUser = mockMvcHelper.get("/api/quiz/bad-id-user",  QuizDetailsDTO.class);
        assertEquals(404, errorFakeUser.status());

    }

    /* Issue 11
    Goal : Be able to change question title or modify the answers
    Specification:
        Endpoint: PUT /api/<quizId>/questions/<questionId>
        Input: {title: 'Question title', answers: [...]}
        Output:
        Status: 204
        Body: none
        Error : 404 if quiz does not exist or does not belong to user
     */
    @Test
    void testChangeTitleQuiz() throws Exception {

        // Récupération du body avec tous les Quiz
        var getRequest = mockMvcHelper.get("/api/quiz", AllQuizUserDTO.class);
        AllQuizUserDTO allQuiz = getRequest.body();

        List<QuizUserDTO> quizUserDTOList = allQuiz.data();
        QuizUserDTO firstQuizUser = quizUserDTOList.get(0);

        // Apple de la route d'un seul quiz
        var getQuizById = mockMvcHelper.get("/api/quiz/" + firstQuizUser.id(),  QuizDetailsDTO.class);
        QuizDetailsDTO quizDetails = getQuizById.body();

        QuestionDTO lastQuestion = quizDetails.questions().get(0);

        QuestionDTO updatedQuizUser = new QuestionDTO(lastQuestion.id(), "New Updated Title", lastQuestion.answers());

        // Requete MockMVC Put pour changer le titre de la question
        var changeTiteQuestion = mockMvcHelper.put(
                "/api/quiz" + firstQuizUser.id() + "/questions/" + lastQuestion.id(),
                updatedQuizUser
                );
        System.out.println("/api/quiz" + firstQuizUser.id() + "/questions/" + lastQuestion.id() + " : " + updatedQuizUser);



//        // Verification cohérence des données
//        assertEquals(firstQuizUser.title(), quizDetails.title());
//        assertEquals(firstQuizUser.description(), quizDetails.description());
//        assertEquals(200, getQuizById.status());
//
//        // Test en cas de mauvais userID ou userId non présent dans la base
//        var errorFakeUser = mockMvcHelper.get("/api/quiz/bad-id-user",  QuizDetailsDTO.class);
//        assertEquals(404, errorFakeUser.status());

    }

    // Fonction Utilitaire pour la récupération de l'id du quiz
    private QuizUserDTO getFirstIdQuiz() throws Exception {
        var getRequest = mockMvcHelper.get("/api/quiz", AllQuizUserDTO.class);
        AllQuizUserDTO allQuiz = getRequest.body();
        List<QuizUserDTO> quizUserDTOList = allQuiz.data();
        return quizUserDTOList.get(0);
    }
}
