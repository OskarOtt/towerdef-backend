package no.oskott.towerdefbackend.run;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaderboardIntegrationTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    private JsonNode startRun() throws Exception {
        String body = mvc.perform(post("/api/runs")).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        return om.readTree(body);
    }

    private String score(String token, String name, int wave) {
        return "{\"token\":\"" + token + "\",\"name\":\"" + name + "\",\"wave\":" + wave
                + ",\"kills\":5,\"topTowerId\":\"blaster\",\"topTowerLevel\":1}";
    }

    @Test
    void fullFlowAndRepeatedSubmits() throws Exception {
        JsonNode run = startRun();
        String id = run.get("runId").asText();
        String token = run.get("token").asText();

        mvc.perform(post("/api/runs/" + id + "/score").contentType(MediaType.APPLICATION_JSON)
                        .content(score(token, "Alice", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank").exists());

        mvc.perform(post("/api/runs/" + id + "/score").contentType(MediaType.APPLICATION_JSON)
                        .content(score(token, "Alice", 1)))
                .andExpect(status().isOk());

        mvc.perform(post("/api/runs/" + id + "/score").contentType(MediaType.APPLICATION_JSON)
                        .content(score(token, "Alice", 1)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/leaderboard?limit=100"))
                .andExpect(jsonPath("$[?(@.name=='Alice')]").value(org.hamcrest.Matchers.hasSize(1)));

        mvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void placementReturnsRankOfRun() throws Exception {
        JsonNode run = startRun();
        String id = run.get("runId").asText();
        mvc.perform(get("/api/leaderboard/run/" + id)).andExpect(status().isNotFound());

        mvc.perform(post("/api/runs/" + id + "/score").contentType(MediaType.APPLICATION_JSON)
                        .content(score(run.get("token").asText(), "Placed", 1)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/leaderboard/run/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Placed"))
                .andExpect(jsonPath("$.rank").isNumber());
    }

    @Test
    void rejectsBadToken() throws Exception {
        JsonNode run = startRun();
        mvc.perform(post("/api/runs/" + run.get("runId").asText() + "/score")
                        .contentType(MediaType.APPLICATION_JSON).content(score("bogus", "Bob", 1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsImplausibleScoreAndInvalidName() throws Exception {
        JsonNode run = startRun();
        mvc.perform(post("/api/runs/" + run.get("runId").asText() + "/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(score(run.get("token").asText(), "Cheater", 1).replace("\"kills\":5", "\"kills\":1000")))
                .andExpect(status().isUnprocessableEntity());

        JsonNode run2 = startRun();
        mvc.perform(post("/api/runs/" + run2.get("runId").asText() + "/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(score(run2.get("token").asText(), "<script>", 1)))
                .andExpect(status().isBadRequest());
    }
}
