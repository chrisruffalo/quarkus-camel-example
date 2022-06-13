package io.github.chrisruffalo.camel.example;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Consume;
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
     * @param messageBody the body of the message received
     */
    @Consume("amqp:topic:done-file")
    public void onFileFinished(final String messageBody) {
        logger.infof("Got notification: %s", messageBody);
    }

}
