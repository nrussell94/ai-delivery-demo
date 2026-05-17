package com.example.demo.config;

import com.example.demo.model.SupportedPostcode;
import com.example.demo.repository.SupportedPostcodeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class SupportedPostcodeSeeder implements ApplicationRunner {

    private static final List<String> DEFAULTS = Stream.concat(
            IntStream.rangeClosed(1, 20).mapToObj(i -> "E" + i),
            IntStream.rangeClosed(1, 28).mapToObj(i -> "SE" + i)
    ).collect(Collectors.toUnmodifiableList());

    private final SupportedPostcodeRepository repository;

    public SupportedPostcodeSeeder(SupportedPostcodeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String code : DEFAULTS) {
            if (!repository.existsById(code)) {
                repository.save(new SupportedPostcode(code));
            }
        }
    }
}
