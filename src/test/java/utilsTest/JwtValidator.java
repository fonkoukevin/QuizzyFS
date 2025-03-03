package utilsTest;

import com.quizzy.quizzy.controller.QuizController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JwtValidator {

    private final MockMvc mockMvc;
    private final Logger logger = LoggerFactory.getLogger(QuizController.class);

    private final String email = "test@email.com";
    private final String userName = "testUser";

    public JwtValidator(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions getMockMvc(String path) {
        try {
            // retourn le mockMVC de connection
            return this.mockMvc.perform(get("/api/quiz")
                    .with(jwt().jwt(jwt -> jwt.claim("sub", userName)
                            .claim("email", email)
                    )));
        } catch (Exception e) {
            // Gérer l'erreur (ex: logger l'erreur et retourner null)
            logger.error("Erreur lors de l'exécution de MockMvc pour le chemin : " + path, e);
            return null; // ⚠️ Attention à bien gérer le `null` dans l'appelant
        }
    }

    //    public JwtValidator loggedAs(String user, Optional<String> email) {
//        this.userName = user;
//        email.ifPresent(s -> this.email = s);
//        return this;
//    }

    //public ApiResponse<T> get<T>(String path){}
    // jwtValidator.get<UserDto>("api/users/me")

    public void performValidation(String path) throws Exception  {

       var request = get(path);
       if (this.userName != null) {
                request = request.with(jwt().jwt(jwt -> jwt.claim("sub", this.userName).claim("email", this.email)));
       }


       mockMvc.perform(request).andExpect(status().isOk());
    }

}
