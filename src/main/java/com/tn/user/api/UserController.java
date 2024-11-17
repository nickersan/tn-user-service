package com.tn.user.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import static com.tn.lang.Strings.isNullOrWhitespace;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tn.query.QueryParseException;
import com.tn.service.IllegalParameterException;
import com.tn.service.query.QueryBuilder;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UserController
{
  private static final QueryBuilder QUERY_BUILDER = new QueryBuilder(User.class);

  private final UserRepository userRepository;

  @GetMapping("/{id}")
  public User userForId(@PathVariable("id") long id)
  {
    return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found for id: " + id));
  }

  @GetMapping
  public Iterable<User> usersFor(@RequestParam(required = false) MultiValueMap<String, String> params)
  {
    try
    {
      String query = QUERY_BUILDER.build(params);

      return isNullOrWhitespace(query) ? userRepository.findAll() : userRepository.findWhere(query);
    }
    catch (IllegalParameterException | QueryParseException e)
    {
      throw new ResponseStatusException(BAD_REQUEST, e.getMessage(), e);
    }
  }

  @PostMapping  
  public User create(@Valid @RequestBody UserRequest request)
  {
    return userRepository.save(new User(request.email, request.firstName, request.lastName, request.tokenSubject));
  }

  
  @PutMapping("/{id}")
  @Transactional
  public User update(@PathVariable("id") long id, @Valid @RequestBody UserRequest request)
  {
    var existingUser = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with ID: " + id));
    
    return userRepository.save(new User(id, request.email, request.firstName, request.lastName, request.tokenSubject, existingUser.created()));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable("id") long id)
  {
    userRepository.deleteById(id);
  }

  public record UserRequest(
    @NotNull(message = "Email required")
    @Email(message = "Email invalid")
    String email,
    @NotNull(message = "First name required")
    String firstName,
    @NotNull(message = "Last name required")
    String lastName,
    String tokenSubject
  ) {}
}
