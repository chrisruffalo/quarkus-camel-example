package io.github.chrisruffalo.camel.example;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Consume;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@RegisterForReflection
public class FileNotificationConsumer {

    @Inject
    Logger logger;

    @Consume("activemq:topic:done-file")
    public void onFileFinished(final String fileName) {
        logger.infof("Got notification for file %s", fileName);
    }

}
