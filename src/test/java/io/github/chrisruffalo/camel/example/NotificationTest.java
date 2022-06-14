package io.github.chrisruffalo.camel.example;

import io.github.chrisruffalo.camel.example.beans.TestWaitBean;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
@Tags({
    @Tag("full")
})
public class NotificationTest extends CommonTest {

    @Inject
    TestWaitBean waitBean;

    @Test
    public void waitForNotification() throws InterruptedException {
        final Response response = this.putData("here is some test data");

        // use response body to get uuid
        final String uuid = response.body().asString().substring("thanks for: ".length()); // simple parse of the uuid from message
        Assertions.assertNotNull(uuid);

        // wait for a notification about the conversation
        waitBean.waitFor(uuid);
    }

}
