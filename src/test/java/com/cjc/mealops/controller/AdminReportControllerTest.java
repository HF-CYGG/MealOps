package com.cjc.mealops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.GlobalExceptionHandler;
import com.cjc.mealops.service.StatsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminReportControllerTest {
    @AfterEach
    void clearContext() {
        BaseContext.clear();
    }

    @Test
    void statsOverviewRejectsUserRole() throws Exception {
        BaseContext.setCurrentRole("USER");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StatsController(org.mockito.Mockito.mock(StatsService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/stats/overview"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Permission denied"));
    }

    @Test
    void logsRejectUserRole() throws Exception {
        BaseContext.setCurrentRole("USER");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OperationLogController(new RecordingApiInvokeSupport()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/logs/page"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Permission denied"));
    }

    @Test
    void hotDishesReturnsBadRequestForInvalidDate() throws Exception {
        BaseContext.setCurrentRole("EMPLOYEE");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StatsController(org.mockito.Mockito.mock(StatsService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/stats/hot-dishes").param("beginTime", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0));
    }

    private static class RecordingApiInvokeSupport extends ApiInvokeSupport {
        private RecordingApiInvokeSupport() {
            super(null, null);
        }
    }
}
