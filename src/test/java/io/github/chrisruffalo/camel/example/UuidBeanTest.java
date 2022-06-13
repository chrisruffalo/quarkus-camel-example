package io.github.chrisruffalo.camel.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UuidBeanTest {

    @Test
    public void generateUuid() {
        UuidBean uuidBean = new UuidBean();
        Assertions.assertNotNull(uuidBean.createUuid());
        Assertions.assertFalse(uuidBean.createUuid().isEmpty());
    }

    @Test
    public void ensureLongEnough() {
        UuidBean uuidBean = new UuidBean();
        Assertions.assertTrue(uuidBean.createUuid().length() > 45);
    }

}
