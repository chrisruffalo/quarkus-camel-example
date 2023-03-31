package io.github.chrisruffalo.camel.example;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@QuarkusIntegrationTest
public class WaitForNotificationIT extends CommonSubmissionTest {

    @Test
    public void testWaitForNotify() throws Exception {
        NotifyBuilder notify;

        // create a camel route that connects to the amqp instance
        try(final DefaultCamelContext context = new DefaultCamelContext()) {
            final RouteBuilder builder = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("amqp:topic:done-file")
                        .log("notified");
                }
            };
            context.addRoutes(builder);

            notify = new NotifyBuilder(context);
        }

        Assertions.assertNotNull(notify);
        notify.from("amqp:topic:done-file")
                .whenCompleted(1)
                .create();

        // make post
        postSubmissionData("here is some data");

        // wait for up to 2 seconds for a match
        notify.matches(2, TimeUnit.SECONDS);
    }

}
