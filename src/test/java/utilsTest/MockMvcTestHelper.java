package utilsTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.HashMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public class MockMvcTestHelper {

    private static final String user = "User";
    private static final String email = "test@email.com";

    private final MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    public MockMvcTestHelper(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    // Methode Post
    public <T> ApiResponse<Void> post(String path, T payload) throws Exception {
        // Fait la connection du client MockMvc
        // Post les données en Json
        // Et retourne le resultat de la reponse HTTP
        var response = mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(payload))
                        .with(jwt().jwt(jwt -> jwt.claim("sub", user).claim("email", email))))
        .andReturn().getResponse();

        // Creation d'une map pour les headers
        var headers = new HashMap<String, String>();
        headers.put("Location", response.getHeader("Location"));

        // Retourne la reponse
        return new ApiResponse<>(response.getStatus(), null, headers);
    }

    // Méthode GET
    public <T> ApiResponse<T> get(String path, Class<T> outputClass) throws Exception {
        // Fait la connection du client MockMvc
        // Et retourne le résultat de la réponse HTML
        var response = mockMvc.perform(MockMvcRequestBuilders.get(path)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", user).claim("email", email))))
                .andReturn().getResponse();

        // Crée une liste vide attendu dans le retour
        var headers = new HashMap<String, String>();

        try {
            // Test si il n'y a pas de problème avec la réponse
            return new ApiResponse<>(response.getStatus(), mapper.readValue(response.getContentAsString(), outputClass), headers);
        } catch (IOException e) {
            // Si la réponse pose problème au Mapper, une instance vide est retourne
            return new ApiResponse<>(response.getStatus(), mapper.readValue("{}", outputClass), headers);
        }
    }

    // Methode Post
    public <T> ApiResponse<Void> patch(String path, T outputClass) throws Exception {
        // Fait la connection du client MockMvc
        // Post les données en Json
        // Et retourne le resultat de la reponse HTTP
        var response = mockMvc.perform(MockMvcRequestBuilders.patch(path)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(outputClass))
                        .with(jwt().jwt(jwt -> jwt.claim("sub", user).claim("email", email))))
                .andReturn().getResponse();

        // Creation d'une map pour les headers
        var headers = new HashMap<String, String>();
        headers.put("Location", response.getHeader("Location"));

        // Retourne la reponse
        return new ApiResponse<>(response.getStatus(), null, headers);
    }

    // Methode Post
    public <T> ApiResponse<Void> put(String path, T outputClass) throws Exception {
        // Fait la connection du client MockMvc
        // Post les données en Json
        // Et retourne le resultat de la reponse HTTP
        var response = mockMvc.perform(MockMvcRequestBuilders.put(path)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(outputClass))
                        .with(jwt().jwt(jwt -> jwt.claim("sub", user).claim("email", email))))
                .andReturn().getResponse();

        // Creation d'une map pour les headers
        var headers = new HashMap<String, String>();
        headers.put("Location", response.getHeader("Location"));

        // Retourne la reponse
        return new ApiResponse<>(response.getStatus(), null, headers);
    }
}