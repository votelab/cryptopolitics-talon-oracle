package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class CardClassEmpty extends CryptopoliticsTalonException {
  public final String cardClass;

  public CardClassEmpty(String cardClass) {
    super(HttpStatus.BAD_REQUEST, cardClass);
    this.cardClass = cardClass;
  }
}
