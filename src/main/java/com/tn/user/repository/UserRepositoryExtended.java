package com.tn.user.repository;

import java.time.Duration;

import com.tn.user.domain.Phone;
import com.tn.user.domain.User;

public interface UserRepositoryExtended
{
  /**
   * Returns the user whose email is {@code email}, or creates a new user with that email,
   * atomically - safe under concurrent calls for the same new value.
   */
  User findOrCreateByEmail(String email);

  /**
   * Returns the user whose phone number is {@code phone}, or creates a new user with that phone
   * number, atomically - safe under concurrent calls for the same new value.
   */
  User findOrCreateByPhone(Phone phone);

  /**
   * Counts an avatar upload the user is starting against the {@code limit} they may start in a {@code window},
   * committing the count at once - even if the upload then fails, so that failed uploads count too. Returns whether
   * the upload may go ahead: false if the user has reached the limit, or isn't there.
   */
  boolean reserveAvatarUpload(long id, int limit, Duration window);

  /**
   * Gives the user the next version of their avatar and returns it. It only goes up, so a version is not reused after
   * an avatar is removed and another set. The user's row stays locked until the transaction ends, which keeps two
   * changes of one user's avatar in order.
   */
  int nextAvatarVersion(long id);

  /** Records that the user has no avatar. */
  void clearAvatarVersion(long id);

  /** Points the user's avatar at an item in tn-media-service, replacing any it had. */
  void setAvatarMedia(long id, long mediaId);

  /** Records that the user has no avatar in tn-media-service. */
  void clearAvatarMedia(long id);
}
