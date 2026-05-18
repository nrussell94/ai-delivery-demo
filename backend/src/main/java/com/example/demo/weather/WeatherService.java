package com.example.demo.weather;

import com.example.demo.weather.exception.LocationOutsideUkException;
import com.example.demo.weather.model.ForecastResponse;
import com.example.demo.weather.model.WarmestDay;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private static final ZoneId LONDON = ZoneId.of("Europe/London");

    private final OpenWeatherClient client;
    private final Clock clock;

    public WeatherService(OpenWeatherClient client, Clock clock) {
        this.client = client;
        this.clock = clock;
    }

    public WarmestDay warmestDay(String lat, String lon) {
        ForecastResponse response = client.fetchForecast(lat, lon);

        // AC4 — UK gating: country must be "GB"
        String country = response.city() != null ? response.city().country() : null;
        if (country == null || country.isBlank() || !country.equals("GB")) {
            throw new LocationOutsideUkException("Location is not within the United Kingdom");
        }

        // Today's date in London local time using the injected clock
        LocalDate today = LocalDate.now(clock.withZone(LONDON));

        // Group slots by London local date, drop today, keep next 5 days
        Map<LocalDate, List<ForecastResponse.ForecastSlot>> byDate = response.list().stream()
                .collect(Collectors.groupingBy(slot ->
                        Instant.ofEpochSecond(slot.dt()).atZone(LONDON).toLocalDate()
                ));

        List<LocalDate> futureDates = byDate.keySet().stream()
                .filter(d -> d.isAfter(today))
                .sorted()
                .limit(5)
                .toList();

        // For each future day, find: max temp, then lowest humidity among slots tied on max temp
        List<WarmestDay> candidates = futureDates.stream()
                .map(date -> pickDayCandidate(date, byDate.get(date)))
                .toList();

        // Sort: temp DESC, humidity ASC, date ASC — take first
        return candidates.stream()
                .min(Comparator
                        .comparingDouble(WarmestDay::temperatureCelsius).reversed()
                        .thenComparingInt(WarmestDay::humidityPercent)
                        .thenComparing(WarmestDay::date))
                .orElseThrow(() -> new IllegalStateException("No future forecast days available"));
    }

    /**
     * For a single day: find the max temperature across all slots, then among the slots
     * that share that max temperature, return the lowest humidity.
     */
    private WarmestDay pickDayCandidate(LocalDate date, List<ForecastResponse.ForecastSlot> slots) {
        double maxTemp = slots.stream()
                .mapToDouble(s -> s.main().temp())
                .max()
                .orElseThrow();

        int minHumidityAtMaxTemp = slots.stream()
                .filter(s -> s.main().temp() == maxTemp)
                .mapToInt(s -> s.main().humidity())
                .min()
                .orElseThrow();

        return new WarmestDay(date, maxTemp, minHumidityAtMaxTemp);
    }
}
