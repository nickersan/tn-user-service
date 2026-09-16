package com.tn.user.api;

import static com.tn.lang.Strings.isNotNullOrWhitespace;
import static net.logstash.logback.argument.StructuredArguments.kv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tn.service.data.parameter.QueryBuilder;
import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/v1/users")
@Tag(name = "Users", description = "User profile CRUD")
public class UserController
{
  private final UserRepository userRepository;
  private final QueryBuilder queryBuilder;

  @GetMapping("/{id}")
  @Operation(summary = "Get a user by id")
  @ApiResponse(responseCode = "200", description = "User found")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  public UserResponse get(@PathVariable Long id)
  {
    return userRepository.findById(id).map(UserResponse::from).orElseThrow(() -> new UserNotFoundException(id));
  }

  @GetMapping
  @Operation(summary = "List/filter users, paginated")
  @ApiResponse(responseCode = "200", description = "Page of users")
  public PagedModel<UserResponse> list(
    @Parameter(description = "tn-query filter expression, validated against this entity's fields") @RequestParam(required = false) String q,
    Pageable pageable
  )
  {
    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

    Page<User> page = isNotNullOrWhitespace(q)
      ? userRepository.findWhere(queryBuilder.build(queryParams(q)), pageRequest)
      : userRepository.findAll(pageRequest);

    return new PagedModel<>(page.map(UserResponse::from));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a user profile")
  @ApiResponse(responseCode = "201", description = "User created")
  @ApiResponse(responseCode = "400", description = "Missing or invalid identifier")
  @ApiResponse(responseCode = "409", description = "A user already exists for this identifier")
  public UserResponse create(@Valid @RequestBody CreateUserRequest request)
  {
    User user = userRepository.save(new User(request.identifierType(), request.identifierValue(), request.fullName(), request.preferredName(), request.tokenSubject()));

    log.info("Created user", kv("identifierType", user.identifierType()), kv("userId", user.id()));

    return UserResponse.from(user);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a user's profile details (identifier is immutable)")
  @ApiResponse(responseCode = "200", description = "User updated")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request)
  {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    User updated = new User(id, user.identifierType(), user.identifierValue(), request.fullName(), request.preferredName(), request.tokenSubject(), user.created());

    return UserResponse.from(userRepository.save(updated));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a user")
  @ApiResponse(responseCode = "204", description = "User deleted")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  public void delete(@PathVariable Long id)
  {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    userRepository.delete(user);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Void> conflict()
  {
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }

  private LinkedMultiValueMap<String, String> queryParams(String q)
  {
    LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("q", q);

    return params;
  }

  public record CreateUserRequest(
    @NotNull @Schema(description = "EMAIL or PHONE") IdentifierType identifierType,
    @NotNull @Schema(description = "The identifier value, in the format matching its type") String identifierValue,
    String fullName,
    String preferredName,
    String tokenSubject
  ) {}

  public record UpdateUserRequest(String fullName, String preferredName, String tokenSubject) {}
}
