package io.inblocks.civicpower.cryptopolitics.exceptions;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class CryptopoliticsTalonException extends HttpStatusException {
  public CryptopoliticsTalonException(HttpStatus status, String message) {
    super(status, message);
  }
}
