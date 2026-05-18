package com.example.demo.weather;

import com.example.demo.weather.exception.UpstreamWeatherException;
import com.example.demo.weather.model.ForecastResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenWeatherClient {

    private final RestTemplate restTemplate;
    private final OpenWeatherProperties properties;

    public OpenWeatherClient(RestTemplate restTemplate, OpenWeatherProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public ForecastResponse fetchForecast(String lat, String lon) {
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl() + "/forecast")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", properties.getApiKey())
                .queryParam("units", "metric")
                .toUriString();

        try {
            ForecastResponse response = restTemplate.getForObject(url, ForecastResponse.class);
            if (response == null) {
                throw new UpstreamWeatherException("No response from weather upstream");
            }
            return response;
        } catch (RestClientException e) {
            throw new UpstreamWeatherException("Upstream weather service error: " + e.getMessage(), e);
        }
    }
}
