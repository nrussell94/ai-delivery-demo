package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("supported_postcodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportedPostcode {

    @Id
    private String code;
}
