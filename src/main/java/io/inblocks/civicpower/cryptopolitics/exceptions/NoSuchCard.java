package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class NoSuchCard extends CryptopoliticsTalonException {
  public final int orderNumber;

  public NoSuchCard(int orderNumber) {
    super(HttpStatus.BAD_REQUEST, "#" + orderNumber);
    this.orderNumber = orderNumber;
  }
}
