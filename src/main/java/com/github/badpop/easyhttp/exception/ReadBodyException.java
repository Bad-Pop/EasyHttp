package com.github.badpop.easyhttp.exception;

import lombok.Getter;

public class ReadBodyException extends RuntimeException {

  @Getter
  private final Object body;

  public ReadBodyException(String message, Object body) {
    super(message);
    this.body = body;
  }

  public ReadBodyException(String message, Object body, Throwable cause) {
    super(message, cause);
    this.body = body;
  }
}
