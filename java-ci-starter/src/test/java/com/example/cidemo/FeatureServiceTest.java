package com.example.cidemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureServiceTest {

    @Test
    void reportsGreetingFeatureDisabledByDefaultWhenConfiguredOff() {
        FeatureService service = new FeatureService(false);

        FeatureService.FeatureStatus status = service.getGreetingFeatureStatus();

        assertEquals("greeting", status.feature());
        assertFalse(status.enabled());
    }

    @Test
    void refusesGreetingWhenFeatureIsDisabled() {
        FeatureService service = new FeatureService(false);

        FeatureDisabledException exception = assertThrows(
                FeatureDisabledException.class,
                () -> service.greet("Ada"));

        assertEquals("The greeting feature is disabled", exception.getMessage());
    }

    @Test
    void greetsNamedUserWhenFeatureIsEnabled() {
        FeatureService service = new FeatureService(true);

        assertEquals("Hello, Ada!", service.greet(" Ada ").message());
    }

    @Test
    void usesWorldWhenNameIsBlankAndFeatureIsEnabled() {
        FeatureService service = new FeatureService(true);

        assertEquals("Hello, World!", service.greet(" ").message());
    }
}
