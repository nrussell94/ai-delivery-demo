package com.example.demo.repository;

import com.example.demo.model.Claim;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClaimRepository extends MongoRepository<Claim, String> {

    List<Claim> findByPolicyIdOrderByCreatedAtDesc(String policyId);
}
