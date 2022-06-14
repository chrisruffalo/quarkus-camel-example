package io.github.chrisruffalo.camel.example;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Consume;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Handles notifications when files are done being processed
 */
@ApplicationScoped
@RegisterForReflection
public class FileNotificationConsumer {

    @Inject
    Logger logger;

    /**
     * Example of a component that listens on a topic as a bean.
     *
     * @param message the observed message
     */
    @Consume("amqp:topic:done-file")
    public void onFileFinished(final Message message) {
        if(message.getHeaders().containsKey("conversationId")) {
            logger.infof("Got notification of conversation %s", message.getHeader("conversationId"));
        } else {
            logger.infof("Got notification for body: %s", message.getBody());
        }
    }

}
