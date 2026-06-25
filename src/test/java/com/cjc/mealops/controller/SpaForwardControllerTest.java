package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpaForwardControllerTest {

    @Test
    void forwardsAdminHistoryRoutesToIndexHtml() {
        SpaForwardController controller = new SpaForwardController();

        assertThat(controller.forwardAdminRoute()).isEqualTo("forward:/index.html");
    }

    @Test
    void forwardsClientHistoryRoutesToIndexHtml() {
        SpaForwardController controller = new SpaForwardController();

        assertThat(controller.forwardClientRoute()).isEqualTo("forward:/index.html");
    }
}
