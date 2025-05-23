package com.example.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ControllerLoggingAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ControllerLoggingAspect loggingAspect;

    @BeforeEach
    public void setUp() {
        loggingAspect.resetCallCount();
    }

    @Test
    public void testAspectCounterIncrement() throws Exception {
        assertEquals(0, loggingAspect.getCallCount(), "Счетчик должен быть равен 0 перед выполнением запроса");

        mockMvc.perform(get("/test/aspect"))
                .andExpect(status().isOk())
                .andExpect(content().string("Тестирование аспекта"));

        assertEquals(2, loggingAspect.getCallCount(), 
                "Счетчик должен увеличиться на 2 после выполнения запроса");
    }

    @Test
    public void testMultipleAspectInvocations() throws Exception {
        assertEquals(0, loggingAspect.getCallCount(), "Счетчик должен быть равен 0 перед выполнением запросов");
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/test/aspect"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Тестирование аспекта"));
        }
        assertEquals(6, loggingAspect.getCallCount(), 
                "Счетчик должен увеличиться на 6 после выполнения 3 запросов");
    }
} 