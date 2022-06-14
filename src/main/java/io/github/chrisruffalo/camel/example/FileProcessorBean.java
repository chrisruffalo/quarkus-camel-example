package io.github.chrisruffalo.camel.example;

import io.quarkus.arc.Priority;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Exchange;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Default (the lowest priority) implementation of the named "file processor" bean. This bean
 * deletes files after processing. This also serves as an example of how to use CDI beans that
 * are wired in to other Quarkus components in Camel routes.
 */
@ApplicationScoped
@Alternative
@Priority(1) // lowest priority
@Named("fileProcessor")
@RegisterForReflection
public class FileProcessorBean implements FileProcessor {

    private static final String CAMEL_FILE_PATH_HEADER = "CamelFileAbsolutePath";

    @Inject
    Logger logger;

    /**
     * Hook to allow subclasses to change post-process behavior
     *
     * @param pathToFile the path to the file that was processed
     */
    protected void postProcess(Path pathToFile) {
        try {
            Files.delete(pathToFile);
        } catch (IOException e) {
            logger.errorf("Could not delete file %s", e);
        }
    }

    public void processFile(final Exchange exchange) {
        Optional<Path> filePath = this.getFilePath(
            exchange.getIn().getBody(),
            exchange.getIn().getHeaders().get(CAMEL_FILE_PATH_HEADER)
        );

        filePath.ifPresent(path -> {
            // restore the conversation id
            String conversationId = path.getFileName().toString();
            if(conversationId.endsWith(".message")) {
                conversationId = conversationId.substring(0, conversationId.length() - ".message".length());
            }
            if (!conversationId.isEmpty()) {
                exchange.getIn().setHeader("conversationId", conversationId);
            }

            // todo: implement processing ???

            // say we are done processing the file
            logger.infof("Done processing file %s", path);

            // end processing
            postProcess(path);
        });

        // keep passing message
        exchange.setMessage(exchange.getIn());
    }

    /**
     * Encapsulates logic for getting an existing file path from
     * a list of potential candidates
     *
     * @param potentialPaths potential values for the file
     * @return an optional containing the path if found, empty optional otherwise
     */
    private Optional<Path> getFilePath(final Object... potentialPaths) {
        return Arrays.stream(potentialPaths)
            .filter(Objects::nonNull)
            .map(Objects::toString)
            .map(Paths::get)
            .filter(Files::exists)
            .findFirst();
    }

}
