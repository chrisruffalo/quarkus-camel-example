package io.github.chrisruffalo.camel.example;


import io.github.chrisruffalo.camel.example.beans.TestFileProcessorBean;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
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
import java.nio.file.WatchService;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Test processing all the way through the file processing bean.
 */
@QuarkusTest
public class FileProcessorBeanTest extends CommonSubmissionTest {

    @Inject
    Logger logger;

    @Inject
    CamelContext context;

    @Inject
    TestFileProcessorBean fileProcessorBean;

    @BeforeEach
    @AfterEach
    public void deleteProcessedFiles() throws IOException {
        try(Stream<Path> fileStream = Files.list(Paths.get("target", "output", ".processed"))) {
            fileStream.filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.deleteIfExists(file);
                    } catch (Exception ex) {
                        logger.errorf("Could not delete %s", file, ex);
                    }
                });
        }
    }

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
     * Given a conversation id wait for the response to appear by waiting
     * for notifications at the process-file route having the matching
     * conversation id.
     *
     * Note: because of the way that camel contexts work this does not
     * work in dev mode (at least that's what it looks like)
     *
     * @param response the response to extract the conversation id from
     */
    private void waitForProcessFileNotify(final Response response) {
        final String uuid = extractConversationIdFromResponse(response);
        Assertions.assertNotNull(uuid);
        Assertions.assertFalse(uuid.isEmpty());

        // notify on process file
        NotifyBuilder notify = new NotifyBuilder(context)
                .from("amqp:queue:process-file")
                .filter((exchange) -> { // there is an idiomatic way to do this using ".when" but I cannot figure out how to import that
                    return uuid.equals(exchange.getIn().getHeader("conversationId", String.class));
                })
                .whenCompleted(1)
                .create();

        // ensure one is received
        Assertions.assertTrue(notify.matches(5, TimeUnit.SECONDS));
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

        final Path expectedPath = Paths.get("target","output",".processed", uuid + ".message").toAbsolutePath().normalize();
        if (Files.exists(expectedPath)) {
            logger.infof("Expected file already exists: %s", expectedPath);
        } else {
            // this handles the race condition that the file isn't done being processed yet
            logger.infof("Waiting for expected file: %s", expectedPath);
            try {
                // register a watch service and wait for the file to appear
                try (final WatchService service = FileSystems.getDefault().newWatchService()) {
                    // this has got to be the weirdest way to wait for a file
                    expectedPath.getParent().register(service, StandardWatchEventKinds.ENTRY_CREATE);
                    final ZonedDateTime totalWaitTime = ZonedDateTime.now().plus(5, ChronoUnit.SECONDS);
                    while(service.poll(1, TimeUnit.SECONDS) != null && !Files.exists(expectedPath) && ZonedDateTime.now().isBefore(totalWaitTime)) {
                        if(Files.exists(expectedPath)) {
                            break;
                        }
                    }
                }
                Assertions.assertTrue(Files.exists(expectedPath));
                Assertions.assertEquals(0, fileProcessorBean.getFailureCount()); // not expecting failures
                Assertions.assertTrue(fileProcessorBean.getSuccessCount() >= 1); // not equal to 1 because sometimes left-over files are processed as well
            } catch (IOException | InterruptedException e) {
                Assertions.fail(e);
            }
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
        waitForProcessFileNotify(response);
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
        waitForProcessFileNotify(response);
        waitForFileToAppear(response);
    }
}
