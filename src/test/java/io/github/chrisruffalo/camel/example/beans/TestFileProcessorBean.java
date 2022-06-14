package io.github.chrisruffalo.camel.example.beans;

import io.github.chrisruffalo.camel.example.FileProcessorBean;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
@Alternative
@Priority(99) // set higher priority so it is picked up during test case
@Named("fileProcessor")
@RegisterForReflection
public class TestFileProcessorBean extends FileProcessorBean {

    @Inject
    Logger logger;

    // keep track of failures
    private long failures = 0;

    // keep track of successes
    private long success = 0;

    public long getFailureCount() {
        return failures;
    }

    public long getSuccessCount() {
        return success;
    }

    public void resetCounts() {
        this.success = 0;
        this.failures = 0;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get("target", "output", ".processed"));
    }

    @Override
    protected void postProcess(Path pathToFile) {
        if (!Files.exists(pathToFile)) {
            failures++;
            return;
        }

        final Path processed = pathToFile.getParent().resolve(".processed").resolve(pathToFile.getFileName());
        try {
            Files.move(pathToFile, processed);
            logger.infof("Moved processed file to %s", processed);
        } catch (IOException e) {
            // no need to be subtle or log this in a test case, try to fail instantly
            failures++;
            throw new RuntimeException(e);
        }

        success++;
    }
}
