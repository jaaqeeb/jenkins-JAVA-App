package com.example.cidemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureController.class)
class FeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeatureService featureService;

    @Test
    void exposesFeatureStatus() throws Exception {
        given(featureService.getGreetingFeatureStatus())
                .willReturn(new FeatureService.FeatureStatus("greeting", false));

        mockMvc.perform(get("/api/features/greeting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feature").value("greeting"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void returnsGreetingWhenFeatureIsEnabled() throws Exception {
        given(featureService.greet("Ada"))
                .willReturn(new FeatureService.GreetingResponse("Hello, Ada!"));

        mockMvc.perform(get("/api/greet").param("name", "Ada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Ada!"));
    }
}
