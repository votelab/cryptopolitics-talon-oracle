package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class NoSuchCardClass extends CryptopoliticsTalonException {
  public final String cardClass;

  public NoSuchCardClass(String cardClass) {
    super(HttpStatus.BAD_REQUEST, "No such card class: " + cardClass);
    this.cardClass = cardClass;
  }
}
