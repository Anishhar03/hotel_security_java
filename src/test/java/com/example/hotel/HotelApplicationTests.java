package com.example.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "hotel.storage.rooms-file=target/test-data/rooms-test.txt",
        "hotel.storage.audit-file=target/test-data/audit-test.txt"
})
@AutoConfigureMockMvc
class HotelApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicPingWorksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void roomApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanListRoomsAndStats() throws Exception {
        mockMvc.perform(get("/api/rooms").header(HttpHeaders.AUTHORIZATION, basic("staff", "staff123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/rooms/stats").header(HttpHeaders.AUTHORIZATION, basic("staff", "staff123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.roomsByStatus.AVAILABLE").exists());
    }

    @Test
    void adminCanCreateChangeAuditAndDeleteRoom() throws Exception {
        String room = """
                {
                  "number": "990",
                  "type": "Penthouse",
                  "status": "AVAILABLE",
                  "floor": 9,
                  "pricePerNight": 7500,
                  "occupantName": "",
                  "notes": "Smoke test room"
                }
                """;

        mockMvc.perform(post("/api/admin/rooms")
                        .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(room))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("990"))
                .andExpect(jsonPath("$.floor").value(9));

        String statusChange = """
                {
                  "status": "OCCUPIED",
                  "occupantName": "Test Guest",
                  "notes": "Checked in by automated test"
                }
                """;

        mockMvc.perform(patch("/api/admin/rooms/990/status")
                        .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusChange))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OCCUPIED"))
                .andExpect(jsonPath("$.occupantName").value("Test Guest"));

        mockMvc.perform(get("/api/admin/audit")
                        .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(delete("/api/admin/rooms/990")
                        .header(HttpHeaders.AUTHORIZATION, basic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    private String basic(String username, String password) {
        String token = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
