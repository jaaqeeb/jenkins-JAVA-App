package com.example.cidemo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestController
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @GetMapping("/api/features/greeting")
    public FeatureService.FeatureStatus greetingFeatureStatus() {
        return featureService.getGreetingFeatureStatus();
    }

    @GetMapping("/api/greet")
    public FeatureService.GreetingResponse greet(
            @RequestParam(required = false) String name) {
        return featureService.greet(name);
    }

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<ApiError> handleFeatureDisabled(FeatureDisabledException exception) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(new ApiError("FEATURE_DISABLED", exception.getMessage()));
    }

    public record ApiError(String code, String message) {}
}
