package com.tn.user.controller;

import static com.tn.lang.Strings.isNotNullOrWhitespace;
import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import com.tn.user.api.MissingIdentifierException;
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
    boolean hasEmail = isNotNullOrWhitespace(request.email());
    boolean hasPhone = request.phone() != null;

    if (hasEmail == hasPhone) throw new MissingIdentifierException("Supply either an email or a phone number");

    User user = hasEmail ? userRepository.findOrCreateByEmail(request.email()) : userRepository.findOrCreateByPhone(request.phone());

    log.info("Find-or-create resolved", kv("identifierType", hasEmail ? "EMAIL" : "PHONE"), kv("userId", user.id()));

    return user;
  }
}
