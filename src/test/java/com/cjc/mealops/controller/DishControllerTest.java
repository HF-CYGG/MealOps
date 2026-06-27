package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.common.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DishControllerTest {

    private RecordingApiInvokeSupport api;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        api = new RecordingApiInvokeSupport();
        mockMvc = MockMvcBuilders.standaloneSetup(new DishController(api))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateStatusAcceptsSingleIdParameter() throws Exception {
        mockMvc.perform(post("/dish/status/0").param("id", "3001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(api.beanName).isEqualTo("dishService");
        assertThat(api.methodNames).containsExactly("updateStatus", "startOrStop", "status", "setStatus");
        assertThat(api.args).containsExactly(0, List.of(3001L));
    }

    @Test
    void updateStatusAcceptsIdsParameterBeforeSingleId() throws Exception {
        mockMvc.perform(post("/dish/status/0")
                        .param("id", "9999")
                        .param("ids", "3001,3002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(api.args).containsExactly(0, List.of(3001L, 3002L));
    }

    @Test
    void updateStatusReturnsBadRequestWhenIdAndIdsAreMissing() throws Exception {
        mockMvc.perform(post("/dish/status/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateStatusReturnsBadRequestWhenParsedIdsAreEmpty() throws Exception {
        mockMvc.perform(post("/dish/status/0").param("ids", ","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateStatusReturnsBadRequestWhenStatusIsInvalid() throws Exception {
        mockMvc.perform(post("/dish/status/2").param("id", "3001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteReturnsBadRequestWhenServiceRejectsEnabledDish() throws Exception {
        api.nextException = new BusinessException("Enabled dishes cannot be deleted");

        mockMvc.perform(delete("/dish").param("ids", "3001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Enabled dishes cannot be deleted"));

        assertThat(api.beanName).isEqualTo("dishService");
        assertThat(api.methodNames).containsExactly("deleteBatch", "delete", "removeBatchByIds");
        assertThat(api.args).containsExactly(List.of(3001L));
    }

    private static class RecordingApiInvokeSupport extends ApiInvokeSupport {
        private String beanName;
        private List<String> methodNames;
        private List<Object> args;
        private RuntimeException nextException;

        private RecordingApiInvokeSupport() {
            super(null, null);
        }

        @Override
        public Object invoke(String beanName, List<String> methodNames, Object... args) {
            this.beanName = beanName;
            this.methodNames = methodNames;
            this.args = List.of(args);
            if (nextException != null) {
                throw nextException;
            }
            return null;
        }
    }
}
