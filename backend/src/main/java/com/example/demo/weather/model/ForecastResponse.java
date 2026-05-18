package com.example.demo.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ForecastResponse(List<ForecastSlot> list, City city) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastSlot(
            long dt,
            @JsonProperty("dt_txt") String dtTxt,
            SlotMain main
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SlotMain(double temp, int humidity) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record City(String country) {}
}
