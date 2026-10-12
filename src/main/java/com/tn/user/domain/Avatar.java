package com.tn.user.domain;

/** An avatar picture as it is stored and served: a JPEG or PNG, re-encoded from the pixels alone. */
public record Avatar(String contentType, byte[] image)
{
}
