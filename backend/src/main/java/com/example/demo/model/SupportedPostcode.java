package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("supported_postcodes")
public class SupportedPostcode {

    @Id
    private String code;

    public SupportedPostcode() {
    }

    public SupportedPostcode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
