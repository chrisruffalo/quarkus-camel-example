package io.github.chrisruffalo.camel.example;


import io.quarkus.deployment.dev.QuarkusDevModeLauncher;
import io.quarkus.runtime.Quarkus;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class FileProcessorBeanTest {

    @Inject
    Logger logger;

    @Inject
    TestFileProcessorBean fileProcessorBean;

    @BeforeEach
    public void resetCounts() {
        fileProcessorBean.resetCounts();
    }

    @Test
    public void putFile() throws InterruptedException {
        final Response response = RestAssured.given().contentType("application/json")
                .body("here is some data")
                .put("/submission")
                .then()
                .statusCode(200)
                .extract().response();
    }

    @Test
    @Tags({
        @Tag("full") // do not run in dev mode
    })
    public void putFileFull() throws InterruptedException, IOException {
        final Response response = RestAssured.given().contentType("application/json")
                .body("here is some data")
                .put("/submission")
                .then()
                .statusCode(200)
                .extract().response();

        // use response body to get uuid
        final String uuid = response.body().asString().substring("thanks for: ".length()); // simple parse of the uuid from message
        Assertions.assertNotNull(uuid);

        final Path expectedPath = Paths.get("target","output",".processed", uuid + ".message");
        logger.infof("Waiting for expected file: %s", expectedPath);

        // register a watch service and wait for the file to appear
        try(final WatchService service = FileSystems.getDefault().newWatchService()) {
            expectedPath.getParent().register(service, StandardWatchEventKinds.ENTRY_CREATE);
            final WatchKey watch = service.poll(5, TimeUnit.SECONDS);
            if (watch != null) {
                watch.pollEvents().forEach(event -> {
                    if (Files.exists(expectedPath)) {
                        watch.cancel();
                    }
                });
                watch.reset();
            }
        }

        Assertions.assertTrue(Files.exists(expectedPath));
        Assertions.assertEquals(0, fileProcessorBean.getFailureCount());
        Assertions.assertTrue(fileProcessorBean.getSuccessCount() >= 1); // not equal to 1 because sometimes left-over files are processed as well
    }

}
