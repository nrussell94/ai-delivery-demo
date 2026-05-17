package com.example.demo.controller;

import com.example.demo.api.QuotesApi;
import com.example.demo.api.model.QuoteRequest;
import com.example.demo.api.model.QuoteResponse;
import com.example.demo.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuotesController implements QuotesApi {

    private final QuoteService quoteService;

    public QuotesController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public ResponseEntity<QuoteResponse> createQuote(QuoteRequest quoteRequest) {
        var quote = quoteService.createQuote(quoteRequest);
        return ResponseEntity.ok(new QuoteResponse(quote.getId(), quote.getPremium().doubleValue()));
    }
}
