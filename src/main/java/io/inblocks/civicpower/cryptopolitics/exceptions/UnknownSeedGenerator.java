package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class UnknownSeedGenerator extends CryptopoliticsTalonException {
    public final String name;

    public UnknownSeedGenerator(String name) {
        super(HttpStatus.BAD_REQUEST, "Unknown seed generator: " + name);
        this.name = name;
    }
}
