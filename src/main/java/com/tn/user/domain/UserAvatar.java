package com.tn.user.domain;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A user's avatar picture. Deliberately not a part of {@link User}, nor related to it by an association, so that
 * nothing that reads users can read a picture along with them.
 */
@Entity
@Table(name = "user_avatars")
@Cacheable(false)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
@Getter
public class UserAvatar
{
  @Id
  @Column(name = "user_id")
  private Long userId;

  /** {@code image/jpeg} or {@code image/png}: of the image stored here, never of what an upload said it was. */
  @Column(name = "content_type")
  private String contentType;

  private byte[] image;
}
