package com.tn.user.controller;

import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import com.tn.user.api.UserActionsApi;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@Slf4j
@AllArgsConstructor
@RestController
public class UserActionsController implements UserActionsApi
{
  private final UserRepository userRepository;

  @Override
  public User findOrCreate(FindOrCreateRequest request)
  {
    User user = userRepository.findOrCreate(request.identifierType(), request.identifierValue());

    log.info("Find-or-create resolved", kv("identifierType", request.identifierType()), kv("userId", user.id()));

    return user;
  }
}
