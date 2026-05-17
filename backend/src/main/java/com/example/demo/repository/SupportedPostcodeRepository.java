package com.example.demo.repository;

import com.example.demo.model.SupportedPostcode;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SupportedPostcodeRepository extends MongoRepository<SupportedPostcode, String> {
}
