package com.tn.user.controller;

import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import com.tn.user.api.AvatarMediaApi;
import com.tn.user.api.UserNotFoundException;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@Slf4j
@AllArgsConstructor
@RestController
public class AvatarMediaController implements AvatarMediaApi
{
  private final UserRepository userRepository;

  @Override
  public User set(Long id, Long mediaId)
  {
    requireUser(id);
    userRepository.setAvatarMedia(id, mediaId);
    log.info("Set avatar media", kv("userId", id), kv("mediaId", mediaId));

    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }

  @Override
  public User remove(Long id)
  {
    requireUser(id);
    userRepository.clearAvatarMedia(id);
    log.info("Cleared avatar media", kv("userId", id));

    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }

  private void requireUser(long id)
  {
    if (!userRepository.existsById(id)) throw new UserNotFoundException(id);
  }
}
