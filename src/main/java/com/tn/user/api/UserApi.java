package com.tn.user.api;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.tn.user.domain.User;

@RequestMapping("/v1/users")
@Tag(name = "Users", description = "User profile CRUD")
public interface UserApi
{
  @GetMapping("/{id}")
  @Operation(summary = "Get a user by id")
  @ApiResponse(responseCode = "200", description = "User found")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  User get(@PathVariable Long id);

  @GetMapping
  @Operation(summary = "List/filter users, paginated")
  @ApiResponse(responseCode = "200", description = "Page of users")
  PagedModel<User> list(
    @Parameter(description = "tn-query filter expression, validated against this entity's fields")
    @RequestParam(required = false)
    String q,
    Pageable pageable
  );

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a user profile - the JSON representation of the user, minus its id")
  @ApiResponse(responseCode = "201", description = "User created")
  @ApiResponse(responseCode = "400", description = "Neither an email nor a phone number was supplied")
  @ApiResponse(responseCode = "409", description = "A user already exists for this email or phone number")
  User create(@Valid @RequestBody UserRequest request);

  @PutMapping("/{id}")
  @Operation(summary = "Replace a user's profile - the JSON representation of the user, minus its id, which is taken from the path")
  @ApiResponse(responseCode = "200", description = "User updated")
  @ApiResponse(responseCode = "400", description = "Neither an email nor a phone number was supplied")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  @ApiResponse(responseCode = "409", description = "A different user already exists for this email or phone number")
  User update(@PathVariable Long id, @Valid @RequestBody UserRequest request);

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a user")
  @ApiResponse(responseCode = "204", description = "User deleted")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  void delete(@PathVariable Long id);

  record UserRequest(
    @Schema(description = "An email address linked to this user, if any")
    String email,
    @Schema(description = "A phone number linked to this user, if any")
    String phone,
    String fullName,
    String preferredName
  )
  {}
}
