package com.tn.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * A phone number as its country calling code and its national significant number - the number
 * without the trunk prefix used when dialling within the country (the UK's leading 0), but keeping
 * any leading 0 that is part of the number (Italy's). Both parts are set or neither is.
 */
@Embeddable
@ValidPhone
public record Phone(
  @Column(name = "phone_country_code")
  Integer countryCode,
  @Column(name = "phone_number", length = 17)
  String number
)
{}
