package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;

class SpaForwardControllerTest {

    @Test
    void forwardsAdminHistoryRoutesToIndexHtml() {
        SpaForwardController controller = new SpaForwardController();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(controller.forwardAdminRoute(response)).isEqualTo("forward:/index.html");
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, max-age=0, must-revalidate");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(response.getHeader("Expires")).isEqualTo("0");
    }

    @Test
    void forwardsClientHistoryRoutesToIndexHtml() {
        SpaForwardController controller = new SpaForwardController();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(controller.forwardClientRoute(response)).isEqualTo("forward:/index.html");
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, max-age=0, must-revalidate");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(response.getHeader("Expires")).isEqualTo("0");
    }

    @Test
    void clientAddressRouteIsCoveredBySpringSpaFallback() throws Exception {
        GetMapping mapping = SpaForwardController.class
                .getDeclaredMethod("forwardClientRoute", jakarta.servlet.http.HttpServletResponse.class)
                .getAnnotation(GetMapping.class);

        assertThat(Arrays.asList(mapping.value())).contains("/client/address");
    }
}
