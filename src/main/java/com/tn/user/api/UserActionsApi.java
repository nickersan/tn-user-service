package com.tn.user.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tn.user.domain.IdentifierType;
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
    @NotNull
    @Schema(description = "EMAIL or PHONE")
    IdentifierType identifierType,
    @NotNull
    @Schema(description = "The identifier value, in the format matching its type")
    String identifierValue
  )
  {}
}
