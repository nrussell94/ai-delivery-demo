package com.example.demo.repository;

import com.example.demo.model.Policy;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PolicyRepository extends MongoRepository<Policy, String> {
    Optional<Policy> findByQuoteId(String quoteId);
}
