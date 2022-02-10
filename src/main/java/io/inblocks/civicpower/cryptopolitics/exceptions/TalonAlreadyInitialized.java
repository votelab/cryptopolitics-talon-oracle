package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class TalonAlreadyInitialized extends CryptopoliticsTalonException {
    public TalonAlreadyInitialized() {
        super(HttpStatus.CONFLICT, "Talon was already initialized");
    }
}
