package com.tn.user.api;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tn.user.domain.Phone;
import com.tn.user.domain.User;

@RequestMapping("/v1/actions")
@Tag(name = "Actions", description = "Operations that don't map onto a single resource verb")
public interface UserActionsApi
{
  @PostMapping("/find-or-create")
  @Operation(summary = "Return the existing profile for an identifier, or create one if none exists")
  @ApiResponse(responseCode = "200", description = "The existing or newly created user")
  @ApiResponse(responseCode = "400", description = "Missing or invalid identifier")
  User findOrCreate(@Valid @RequestBody FindOrCreateRequest request);

  record FindOrCreateRequest(
    @Schema(description = "An email address - supply this or a phone number, not both")
    String email,
    @Schema(description = "A phone number as a country calling code and a national number without its trunk prefix - supply this or an email address, not both")
    @Valid
    Phone phone
  )
  {}
}
