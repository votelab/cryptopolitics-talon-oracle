package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;

public class NoSuchCardSerie extends CryptopoliticsTalonException {
  public final String name;

  public NoSuchCardSerie(String name) {
    super(HttpStatus.BAD_REQUEST, "No such card serie: " + name);
    this.name = name;
  }
}
