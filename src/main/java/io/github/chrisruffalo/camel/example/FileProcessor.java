package io.github.chrisruffalo.camel.example;

import org.apache.camel.Exchange;

/**
 * Interface bean for file processor implementations
 */
public interface FileProcessor {

    /**
     * How to handle the exchange when (potentially) processing a file
     *
     * @param exchange the in/out exchange
     */
    void processFile(final Exchange exchange);

}
