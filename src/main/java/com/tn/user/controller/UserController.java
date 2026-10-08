package com.tn.user.controller;

import static com.tn.lang.Strings.isNotNullOrWhitespace;
import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.tn.service.data.parameter.QueryBuilder;
import com.tn.user.api.MissingIdentifierException;
import com.tn.user.api.UserApi;
import com.tn.user.api.UserNotFoundException;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@Slf4j
@AllArgsConstructor
@RestController
public class UserController implements UserApi
{
  private final UserRepository userRepository;
  private final QueryBuilder queryBuilder;

  @Override
  public User get(Long id)
  {
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }

  @Override
  public PagedModel<User> list(String q, Pageable pageable)
  {
    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

    Page<User> page = isNotNullOrWhitespace(q)
      ? userRepository.findWhere(queryBuilder.build(queryParams(q)), pageRequest)
      : userRepository.findAll(pageRequest);

    return new PagedModel<>(page);
  }

  @Override
  public User create(UserRequest request)
  {
    requireIdentifier(request);

    User user = userRepository.save(new User(request.email(), request.phone(), request.fullName(), request.preferredName()));

    log.info("Created user", kv("userId", user.id()));

    return user;
  }

  @Override
  public User update(Long id, UserRequest request)
  {
    requireIdentifier(request);

    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    User updated = new User(id, request.email(), request.phone(), request.fullName(), request.preferredName(), user.created());

    return userRepository.save(updated);
  }

  @Override
  public void delete(Long id)
  {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    userRepository.delete(user);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Void> conflict()
  {
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }

  private void requireIdentifier(UserRequest request)
  {
    if (!isNotNullOrWhitespace(request.email()) && !isNotNullOrWhitespace(request.phone())) throw new MissingIdentifierException();
  }

  private LinkedMultiValueMap<String, String> queryParams(String q)
  {
    LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("q", q);

    return params;
  }
}
