package utilsTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public class MockMvcTestHelper {

    private static final String user = "User";
    private static final String email = "test@email.com";

    // Méthode statique pour exécuter une requête MockMvc
    public String performRequest(MockMvc mockMvc, String path, String httpMethod, String bodyContent, int expectedStatus) throws Exception {
            final String user = "User";
            final String email = "test@email.com";

            String body;

            if ("POST".equalsIgnoreCase(httpMethod)) {
                body = mockMvc.perform(MockMvcRequestBuilders.post(path)
                                .with(jwt().jwt(jwt -> jwt.claim("sub", user).claim("email", email))) // Ici !
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyContent))
                        .andExpect(MockMvcResultMatchers.status().is(expectedStatus))
                        //.andExpect(MockMvcResultMatchers.content().json(bodyContent))
                        .andReturn().getResponse().getContentAsString();

            } else {
                body = "test";
                mockMvc.perform(MockMvcRequestBuilders.get(path)
                                .with(jwt().jwt(jwt -> jwt.claim("sub", user).claim("email", email)))) // Ici aussi !
                        .andExpect(MockMvcResultMatchers.status().is(404));
                        //.andReturn().getResponse().getContentAsString();
            }
            System.out.println("Return  body classHelper " + body);
            return body;
        }
}
