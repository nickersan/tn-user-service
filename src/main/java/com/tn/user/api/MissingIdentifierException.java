package com.tn.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingIdentifierException extends RuntimeException
{
  public MissingIdentifierException()
  {
    super("A user must have an email or a phone number");
  }
}
