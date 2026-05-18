package com.example.demo.weather.model;

import java.time.LocalDate;

public record WarmestDay(LocalDate date, double temperatureCelsius, int humidityPercent) {
}
