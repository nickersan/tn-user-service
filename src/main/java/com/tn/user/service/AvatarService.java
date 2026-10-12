package com.tn.user.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.tn.user.api.AvatarNotFoundException;
import com.tn.user.api.TooManyAvatarUploadsException;
import com.tn.user.api.UserNotFoundException;
import com.tn.user.config.AvatarProperties;
import com.tn.user.domain.Avatar;
import com.tn.user.domain.User;
import com.tn.user.domain.UserAvatar;
import com.tn.user.repository.UserAvatarRepository;
import com.tn.user.repository.UserRepository;

/**
 * Setting, reading and removing a user's avatar. Not transactional as a whole: an upload is counted against the user's
 * limit in a transaction of its own, so that it counts even when it is then refused, and the picture is only written
 * once it has been made safe.
 */
@Slf4j
@Service
@AllArgsConstructor
public class AvatarService
{
  private final UserRepository userRepository;
  private final UserAvatarRepository userAvatarRepository;
  private final AvatarImages images;
  private final AvatarProperties properties;
  private final TransactionTemplate transaction;

  /** Replaces the user's avatar with the upload, once made safe, and returns the user with their new avatar version. */
  public User set(long userId, byte[] upload)
  {
    requireUser(userId);

    if (!userRepository.reserveAvatarUpload(userId, properties.uploadLimit(), properties.uploadWindow()))
    {
      throw new TooManyAvatarUploadsException("Too many avatars uploaded; try again later");
    }

    Avatar avatar = images.process(upload);

    User user = transaction.execute(status ->
    {
      userRepository.nextAvatarVersion(userId);
      userAvatarRepository.save(new UserAvatar(userId, avatar.contentType(), avatar.image()));

      return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    });

    log.info("Set avatar", kv("userId", userId), kv("contentType", avatar.contentType()), kv("bytes", avatar.image().length), kv("version", user.avatarVersion()));

    return user;
  }

  public Avatar get(long userId)
  {
    requireUser(userId);

    UserAvatar avatar = userAvatarRepository.findById(userId).orElseThrow(() -> new AvatarNotFoundException(userId));

    return new Avatar(avatar.contentType(), avatar.image());
  }

  /** Removes the user's avatar, if they have one. */
  public void remove(long userId)
  {
    requireUser(userId);

    transaction.executeWithoutResult(status ->
    {
      userAvatarRepository.deleteById(userId);
      userRepository.clearAvatarVersion(userId);
    });

    log.info("Removed avatar", kv("userId", userId));
  }

  private void requireUser(long userId)
  {
    if (!userRepository.existsById(userId)) throw new UserNotFoundException(userId);
  }
}
