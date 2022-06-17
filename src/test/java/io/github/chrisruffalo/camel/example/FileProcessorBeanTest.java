package io.github.chrisruffalo.camel.example;


import io.github.chrisruffalo.camel.example.beans.TestFileProcessorBean;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

/**
 * Test processing all the way through the file processing bean.
 */
@QuarkusTest
public class FileProcessorBeanTest extends CommonSubmissionTest {

    @Inject
    Logger logger;

    @Inject
    TestFileProcessorBean fileProcessorBean;

    @BeforeEach
    public void resetCounts() {
        fileProcessorBean.resetCounts();
    }

    /**
     * Put and wait for events to fire
     */
    @Test
    public void putFile() {
        this.putSubmissionData("some data");
    }

    /**
     * Post and wait for events to fire
     */
    @Test
    public void postFile() {
        this.postSubmissionData("some data");
    }

    /**
     * Wait for the file to appear based on the conversation id.
     *
     * @param response the response to extract the conversation id from
     */
    private void waitForFileToAppear(final Response response) {
        // use response body to get uuid
        final String uuid = extractConversationIdFromResponse(response); // simple parse of the uuid from message
        Assertions.assertNotNull(uuid);
        Assertions.assertFalse(uuid.isEmpty());

        final Path expectedPath = Paths.get("target","output",".processed", uuid + ".message");
        logger.infof("Waiting for expected file: %s", expectedPath);

        try {
            // register a watch service and wait for the file to appear
            try (final WatchService service = FileSystems.getDefault().newWatchService()) {
                expectedPath.getParent().register(service, StandardWatchEventKinds.ENTRY_CREATE);
                final WatchKey watch = service.poll(5, TimeUnit.SECONDS); // a five-second timeout is extremely generous here
                Assertions.assertNotNull(watch);
                Assertions.assertTrue(watch.pollEvents().stream()
                    // the event check conditional is overkill but we want to make sure that we are triggering off the result for the
                    // actual file we are looking for and not a related event or spurious event from leftover files being processed
                    .map(event -> event.context() instanceof Path && event.context().equals(expectedPath.getFileName()) && Files.exists(expectedPath))
                    .filter(result -> result)
                    .findFirst().orElse(false)
                );
                watch.reset();
            }

            Assertions.assertEquals(0, fileProcessorBean.getFailureCount()); // not expecting failures
            Assertions.assertTrue(fileProcessorBean.getSuccessCount() >= 1); // not equal to 1 because sometimes left-over files are processed as well
        } catch (IOException | InterruptedException e) {
            Assertions.fail(e);
        }
    }

    /**
     * Post but using alternate bean that moves the file, wait for file to appear
     */
    @Test
    @Tags({
        @Tag("full") // do not run in dev mode
    })
    public void postAndWaitForFileToAppear() {
        final Response response = this.postSubmissionData("here is some test data");
        waitForFileToAppear(response);
    }

    /**
     * Put but using alternate bean that moves the file, wait for file to appear
     */
    @Test
    @Tags({
        @Tag("full") // do not run in dev mode
    })
    public void putAndWaitForFileToAppear() {
        final Response response = this.putSubmissionData("here is some test data");
        waitForFileToAppear(response);
    }
}
