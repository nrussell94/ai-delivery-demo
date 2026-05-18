package com.example.demo.weather;

import com.example.demo.api.WeatherApi;
import com.example.demo.api.model.WarmestDayResponse;
import com.example.demo.weather.model.WarmestDay;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController implements WeatherApi {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public ResponseEntity<WarmestDayResponse> getWarmestDay(String lat, String lon) {
        WarmestDay warmestDay = weatherService.warmestDay(lat, lon);
        WarmestDayResponse response = new WarmestDayResponse();
        response.setDate(warmestDay.date());
        response.setTemperatureCelsius((double) warmestDay.temperatureCelsius());
        response.setHumidityPercent(warmestDay.humidityPercent());
        return ResponseEntity.ok(response);
    }
}
