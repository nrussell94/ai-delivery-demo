package com.example.demo.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "openweather.api-key=test-key",
        "openweather.base-url=https://api.openweathermap.org/data/2.5",
        "spring.main.allow-bean-definition-overriding=true"
})
class WeatherControllerIT {

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        public Clock weatherClock() {
            // Fixed to 2026-05-18T09:00:00Z → London local time 10:00 BST → today = 2026-05-18
            return Clock.fixed(Instant.parse("2026-05-18T09:00:00Z"), ZoneId.of("Europe/London"));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    /**
     * D1 (2026-05-19): temps 15.0 / 17.0°C → max 17.0
     * D2 (2026-05-20): temps 18.5 (hum 70%) / 16.0°C → max 18.5, hum 70
     * D3 (2026-05-21): temps 18.5 (hum 65%) / 18.5 (hum 80%) → max 18.5, lowest hum at max = 65
     * D4 (2026-05-22): temps 18.5 (hum 65%) / 17.0°C → max 18.5, hum 65
     * D5 (2026-05-23): temps 18.0 / 16.0°C → max 18.0
     *
     * Expected winner: D3 — ties D2 on temp (18.5) but lower humidity (65 < 70);
     *                        ties D4 on temp+humidity (18.5/65) but earlier date.
     */
    @Test
    void returnsWarmestDayWithTieBreakers() throws Exception {
        // Epochs (UTC):
        // D1: 2026-05-19 08:00Z (09:00 BST) = 1779177600, 14:00Z (15:00 BST) = 1779199200
        // D2: 2026-05-20 08:00Z = 1779264000, 14:00Z = 1779285600
        // D3: 2026-05-21 08:00Z = 1779350400, 14:00Z = 1779372000
        // D4: 2026-05-22 08:00Z = 1779436800, 14:00Z = 1779458400
        // D5: 2026-05-23 08:00Z = 1779523200, 14:00Z = 1779544800
        String forecastBody = """
                {
                  "list": [
                    {"dt": 1779177600, "dt_txt": "2026-05-19 08:00:00", "main": {"temp": 15.0, "humidity": 55}},
                    {"dt": 1779199200, "dt_txt": "2026-05-19 14:00:00", "main": {"temp": 17.0, "humidity": 60}},
                    {"dt": 1779264000, "dt_txt": "2026-05-20 08:00:00", "main": {"temp": 18.5, "humidity": 70}},
                    {"dt": 1779285600, "dt_txt": "2026-05-20 14:00:00", "main": {"temp": 16.0, "humidity": 75}},
                    {"dt": 1779350400, "dt_txt": "2026-05-21 08:00:00", "main": {"temp": 18.5, "humidity": 65}},
                    {"dt": 1779372000, "dt_txt": "2026-05-21 14:00:00", "main": {"temp": 18.5, "humidity": 80}},
                    {"dt": 1779436800, "dt_txt": "2026-05-22 08:00:00", "main": {"temp": 18.5, "humidity": 65}},
                    {"dt": 1779458400, "dt_txt": "2026-05-22 14:00:00", "main": {"temp": 17.0, "humidity": 68}},
                    {"dt": 1779523200, "dt_txt": "2026-05-23 08:00:00", "main": {"temp": 18.0, "humidity": 62}},
                    {"dt": 1779544800, "dt_txt": "2026-05-23 14:00:00", "main": {"temp": 16.0, "humidity": 70}}
                  ],
                  "city": {"country": "GB"}
                }
                """;

        mockServer.expect(requestTo(
                "https://api.openweathermap.org/data/2.5/forecast?lat=51.507412&lon=-0.127812&appid=test-key&units=metric"
        )).andRespond(withSuccess(forecastBody, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/weather/warmest-day?lat=51.507412&lon=-0.127812"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-05-21"))
                .andExpect(jsonPath("$.temperatureCelsius").value(18.5))
                .andExpect(jsonPath("$.humidityPercent").value(65));

        mockServer.verify();
    }

    @Test
    void rejectsMoreThanSixDecimalPlaces() throws Exception {
        // lat has 7 decimal places — schema @Pattern should reject before any upstream call
        mockMvc.perform(get("/api/weather/warmest-day?lat=51.5074123&lon=-0.127812"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(not(emptyString())));

        // No stub registered — mockServer.verify() confirms zero upstream calls were made
        mockServer.verify();
    }

    @Test
    void rejectsLocationOutsideUk() throws Exception {
        String parisBody = """
                {
                  "list": [
                    {"dt": 1779264000, "dt_txt": "2026-05-20 08:00:00", "main": {"temp": 22.0, "humidity": 55}}
                  ],
                  "city": {"country": "FR"}
                }
                """;

        mockServer.expect(requestTo(
                "https://api.openweathermap.org/data/2.5/forecast?lat=48.85&lon=2.35&appid=test-key&units=metric"
        )).andRespond(withSuccess(parisBody, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/weather/warmest-day?lat=48.85&lon=2.35"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(not(emptyString())));

        // Exactly one upstream call was made
        mockServer.verify();
    }
}
