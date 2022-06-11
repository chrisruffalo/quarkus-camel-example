package io.github.chrisruffalo.camel.example;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@RegisterForReflection
public class UuidBean {

    public String createUuid() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

}
