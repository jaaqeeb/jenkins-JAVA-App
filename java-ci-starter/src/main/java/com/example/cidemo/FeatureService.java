package com.example.cidemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureService {

    private final boolean greetingFeatureEnabled;

    public FeatureService(
            @Value("${app.features.greeting-enabled:false}") boolean greetingFeatureEnabled) {
        this.greetingFeatureEnabled = greetingFeatureEnabled;
    }

    public FeatureStatus getGreetingFeatureStatus() {
        return new FeatureStatus("greeting", greetingFeatureEnabled);
    }

    public GreetingResponse greet(String name) {
        if (!greetingFeatureEnabled) {
            throw new FeatureDisabledException("The greeting feature is disabled");
        }

        String safeName = (name == null || name.isBlank()) ? "World" : name.trim();
        return new GreetingResponse("Hello, " + safeName + "!");
    }

    public record FeatureStatus(String feature, boolean enabled) {}
    public record GreetingResponse(String message) {}
}
