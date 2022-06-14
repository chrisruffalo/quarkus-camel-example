package io.github.chrisruffalo.camel.example.beans;

import org.apache.camel.Message;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Named("testWaitBean")
public class TestWaitBean {

    @Inject
    Logger logger;

    final Map<String, CountDownLatch> waitMap = new ConcurrentHashMap<>();

    public void waitFor(final String conversationId) {
        final CountDownLatch latch = waitMap.computeIfAbsent(conversationId, (cid) -> new CountDownLatch(1));
        try {
            // assert that we wait for the latch
            logger.infof("Waiting for notification about message %s", conversationId);
            Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }
    }

    public void completeWith(final Message message) {
        // require conversation id
        if(!message.getHeaders().containsKey("conversationId")) {
            return;
        }

        final String conversationId = Objects.toString(message.getHeader("conversationId"));
        logger.infof("Completed conversation %s", conversationId);
        waitMap.computeIfAbsent(conversationId, (cid) -> new CountDownLatch(1)).countDown(); // if the key is absent then create one so that it will be found in the other thread
    }

}
