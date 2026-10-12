package com.tn.user.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone
{
  String message() default "must be a valid phone number, as a country code and a national number without any trunk prefix";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
