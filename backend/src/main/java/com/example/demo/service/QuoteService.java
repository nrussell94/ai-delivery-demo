package com.example.demo.service;

import com.example.demo.api.model.QuoteRequest;
import com.example.demo.exception.QuoteRejectedException;
import com.example.demo.model.Quote;
import com.example.demo.repository.QuoteRepository;
import com.example.demo.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;

    public QuoteService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public Quote createQuote(QuoteRequest request) {
        if (!PostcodeRules.isSupported(request.getPostcode())) {
            throw new QuoteRejectedException("Postcode not supported");
        }
        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 21) {
            throw new QuoteRejectedException("Driver must be 21 or older");
        }
        var premium = RatingEngine.computePremium(age, request.getPostcode());
        var quote = new Quote(
                UUID.randomUUID().toString(),
                request.getName(),
                request.getDateOfBirth(),
                request.getDriverLicenceNumber(),
                request.getPostcode(),
                request.getVehicleRegistration(),
                premium,
                null
        );
        return quoteRepository.save(quote);
    }

    public Quote getQuote(String id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quote not found"));
    }
}
