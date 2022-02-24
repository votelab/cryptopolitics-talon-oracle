package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class CardClassFinitudeMismatch extends CryptopoliticsTalonException {
    public final String cardClass;

    public CardClassFinitudeMismatch(String cardClass) {
        super(HttpStatus.BAD_REQUEST, "Finitude mismatch in class " + cardClass);
        this.cardClass = cardClass;
    }
}
