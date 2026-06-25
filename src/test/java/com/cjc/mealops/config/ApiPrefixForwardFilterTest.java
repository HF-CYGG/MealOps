package com.cjc.mealops.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiPrefixForwardFilterTest {

    @Test
    void forwardsApiPrefixedRequestsToExistingBackendRoutes() throws ServletException, IOException {
        ApiPrefixForwardFilter filter = new ApiPrefixForwardFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/employee/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getForwardedUrl()).isEqualTo("/employee/login");
    }

    @Test
    void leavesNonApiRequestsInTheFilterChain() throws ServletException, IOException {
        ApiPrefixForwardFilter filter = new ApiPrefixForwardFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getForwardedUrl()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }
}
