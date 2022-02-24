package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class CardAlreadyPresent extends CryptopoliticsTalonException {
  public final int orderNumber;

  public CardAlreadyPresent(int orderNumber) {
    super(HttpStatus.CONFLICT, "Card already present: #" + orderNumber);
    this.orderNumber = orderNumber;
  }
}
