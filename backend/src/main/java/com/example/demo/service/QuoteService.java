package com.example.demo.service;

import com.example.demo.api.model.QuoteRequest;
import com.example.demo.api.model.QuoteResponse;
import com.example.demo.model.Quote;
import com.example.demo.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QuoteService {

    private final EligibilityService eligibilityService;
    private final RatingService ratingService;
    private final QuoteRepository quoteRepository;

    public QuoteService(EligibilityService eligibilityService,
                        RatingService ratingService,
                        QuoteRepository quoteRepository) {
        this.eligibilityService = eligibilityService;
        this.ratingService = ratingService;
        this.quoteRepository = quoteRepository;
    }

    public QuoteResponse createQuote(QuoteRequest request) {
        eligibilityService.check(request.getDateOfBirth(), request.getPostcode());

        BigDecimal premium = ratingService.price(request.getDateOfBirth(), request.getPostcode());

        Quote quote = new Quote();
        quote.setDriverName(request.getDriverName());
        quote.setDateOfBirth(request.getDateOfBirth());
        quote.setLicenceNumber(request.getLicenceNumber());
        quote.setPostcode(request.getPostcode());
        quote.setVehicleRef(request.getVehicleRef());
        quote.setPremium(premium);

        Quote saved = quoteRepository.save(quote);

        QuoteResponse response = new QuoteResponse();
        response.setQuoteId(saved.getId());
        response.setPremium(saved.getPremium());
        return response;
    }
}
