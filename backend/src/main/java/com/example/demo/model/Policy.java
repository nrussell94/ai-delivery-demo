package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document("policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    private String id;

    @Indexed(unique = true)
    private String quoteId;

    private BigDecimal premium;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @CreatedDate
    private Instant createdAt;
}
