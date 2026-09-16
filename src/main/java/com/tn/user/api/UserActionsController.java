package com.tn.user.api;

import static net.logstash.logback.argument.StructuredArguments.kv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/v1/actions")
@Tag(name = "Actions", description = "Operations that don't map onto a single resource verb")
public class UserActionsController
{
  private final UserRepository userRepository;

  @PostMapping("/find-or-create")
  @Operation(summary = "Return the existing profile for an identifier, or create one if none exists")
  @ApiResponse(responseCode = "200", description = "The existing or newly created user")
  @ApiResponse(responseCode = "400", description = "Missing or invalid identifier")
  public UserResponse findOrCreate(@Valid @RequestBody FindOrCreateRequest request)
  {
    User user = userRepository.findOrCreate(request.identifierType(), request.identifierValue());

    log.info("Find-or-create resolved", kv("identifierType", user.identifierType()), kv("userId", user.id()));

    return UserResponse.from(user);
  }

  public record FindOrCreateRequest(
    @NotNull @Schema(description = "EMAIL or PHONE") IdentifierType identifierType,
    @NotNull @Schema(description = "The identifier value, in the format matching its type") String identifierValue
  ) {}
}
