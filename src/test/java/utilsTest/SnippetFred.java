//package utilsTest;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//record ApiResponseRec<T>(int status, T body, Map<String, String> headers) {}
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class PingControllerTests {
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void testPing() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/ping"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"));
//    }
//
//    @Test
//    void testPing2() throws Exception {
//        var response = mockMvc.perform(MockMvcRequestBuilders.get("/ping")).andReturn().getResponse();
//        var obj = new ApiResponseRec<PingResponse>(response.getStatus(), objectMapper.readValue(response.getContentAsString(), PingResponse.class));
//        assertEquals(200, obj.status());
//        assertEquals(PingStatus.OK, obj.body().status);
//    }
//}