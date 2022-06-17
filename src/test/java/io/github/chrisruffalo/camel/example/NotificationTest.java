package io.github.chrisruffalo.camel.example;

import io.github.chrisruffalo.camel.example.beans.TestWaitBean;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class NotificationTest extends CommonSubmissionTest {

    @Inject
    TestWaitBean waitBean;

    @Inject
    CamelContext context;

    /*
     * Test waiting for a message in a more traditional way (using NotifyBuilder).
     */
    @Test
    public void postSubmissionAndWaitForNotify() {
        this.postSubmissionData("alternate test data");
    }

    /**
     * Using a test-only route file, wait for a notification about the submission by waiting for an exchange
     * with the matching uuid. This is not a great test but it demonstrates the augmentation of unit testing
     * by adding additional routes (adding a route with the testWaitBean that waits for notifications).
     */
    @Tags({
        @Tag("full")
    })
    @Test
    public void waitForNotification() {
        final Response response = this.putSubmissionData("here is some test data");

        // use response body to get uuid
        final String uuid = extractConversationIdFromResponse(response);
        Assertions.assertNotNull(uuid);

        // use notify to test that the event was actually sent, keeping in mind that this does not work in dev mode
        // because of something (seemingly) to do with the camel context
        NotifyBuilder notify = new NotifyBuilder(context)
            .wereSentTo("amqp:topic:done-file")
            .filter((exchange) -> { // there is an idiomatic way to do this using ".when" but I cannot figure out how to import that
                return uuid.equals(exchange.getIn().getHeader("conversationId", String.class));
            })
            .whenCompleted(1)
            .create();

        // ensure one is received
        Assertions.assertTrue(notify.matches(1, TimeUnit.SECONDS));

        // wait for a notification about the conversation in a non-camel way
        waitBean.waitFor(uuid);
    }

}
