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

/**
 * This bean allows the test cases to create a latch that won't open/release until
 * the requested conversation has been observed on the "done" topic. This class
 * operates to handle both waiting for the condition without a busy-loop and
 * handles the race condition between the bean method and the wait method.
 *
 * Cases:
 * - The waitFor() method is called first which causes the latch to be created
 *   and the caller to wait.
 * - The completeWith() method is called first which creates a situation where
 *   any subsequent calls to waitFor() need to complete immediately.
 *
 * Since the cases do not have a guaranteed order and there is no critical section
 * the latch is probably the most straightforward way to do this.
 *
 * Note: there is probably a better way to test this in more idiomatic camel maybe
 * using the NotifyBuilder but this test itself demonstrates a way to bolt on a
 * separate route file to existing routes and it tests the continuity of the
 * conversation id.
 */
@ApplicationScoped
@Named("testWaitBean")
public class TestWaitBean {

    @Inject
    Logger logger;

    final Map<String, CountDownLatch> waitMap = new ConcurrentHashMap<>();

    /**
     * Shared latch getting code that will get or create the latch
     *
     * @param conversationId the conversation id that the latch is meant to observe
     * @return a newly created latch for that conversation, never null
     */
    private CountDownLatch getOrCreateLatch(final String conversationId) {
        return waitMap.computeIfAbsent(conversationId, (cid) -> new CountDownLatch(1));
    }

    /**
     * Attempt to wait for the requested latch to be open for a total of 5 seconds. Fails
     * the test if the latch doesn't open in time.
     *
     * @param conversationId the conversation id of the related latch
     */
    public void waitFor(final String conversationId) {
        final CountDownLatch latch = getOrCreateLatch(conversationId);
        try {
            // assert that we wait for the latch
            logger.infof("Waiting for notification about message %s", conversationId);
            Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }
    }

    /**
     * Bean method for pipeline that unlatches the latch related to the conversation id.
     *
     * @param message the message on the route
     */
    public void completeWith(final Message message) {
        // require conversation id
        if(!message.getHeaders().containsKey("conversationId")) {
            return;
        }

        final String conversationId = Objects.toString(message.getHeader("conversationId"));
        logger.infof("Completed conversation %s", conversationId);
        getOrCreateLatch(conversationId).countDown(); // to unlatch get the latch and count down
    }

}
