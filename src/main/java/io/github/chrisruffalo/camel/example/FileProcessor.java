package io.github.chrisruffalo.camel.example;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Exchange;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@Named("fileProcessor")
@RegisterForReflection
public class FileProcessor {

    private static final String CAMEL_FILE_PATH_HEADER = "CamelFileAbsolutePath";

    @Inject
    Logger logger;

    public void processFile(final Exchange exchange) {
        Optional<Path> filePath = this.getFilePath(
            exchange.getIn().getBody(),
            exchange.getIn().getHeaders().get(CAMEL_FILE_PATH_HEADER)
        );

        filePath.ifPresent( path -> {
            logger.infof("Done processing file %s", path);

            try {
                Files.delete(path);
            } catch (IOException e) {
                logger.errorf("Could not delete file %s", e);
            }
        });

        // keep passing message
        exchange.setMessage(exchange.getIn());
    }

    private Optional<Path> getFilePath(final Object... potentialPaths) {
        return Arrays.stream(potentialPaths)
            .filter(Objects::nonNull)
            .map(Objects::toString)
            .map(Paths::get)
            .filter(Files::exists)
            .findFirst();
    }

}
