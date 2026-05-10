package com.example.demo.controller;

import com.example.demo.api.HealthApi;
import com.example.demo.api.model.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {
    @Override
    public ResponseEntity<HealthResponse> getHealth() {
        return ResponseEntity.ok(new HealthResponse().status("ok"));
    }
}
