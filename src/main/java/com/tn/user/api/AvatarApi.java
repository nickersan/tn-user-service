package com.tn.user.api;

import java.io.InputStream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.tn.user.domain.User;

@RequestMapping("/v1/users/{id}/avatar")
@Tag(name = "Avatars", description = "A user's avatar picture: a JPEG or PNG of at most 100 KB, kept apart from the user so that reading users never reads it")
public interface AvatarApi
{
  @PutMapping
  @Operation(
    summary = "Set or replace a user's avatar",
    description = "The body is the picture itself. It is judged by its contents, not by the content type it is sent with, "
      + "and is stored re-encoded from its pixels alone: nothing from its embedded metadata is kept.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "*/*", schema = @Schema(type = "string", format = "binary")))
  )
  @ApiResponse(responseCode = "200", description = "Avatar set; the user, with the new avatarVersion")
  @ApiResponse(responseCode = "400", description = "Not a JPEG or PNG, larger than 100 KB (before or after re-encoding), or more than 2048 pixels on a side")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  @ApiResponse(responseCode = "429", description = "The user has uploaded avatars too often, or too many are being processed")
  User set(
    @PathVariable Long id,
    @Parameter(hidden = true)
    @RequestHeader(value = HttpHeaders.CONTENT_LENGTH, required = false)
    Long contentLength,
    @Parameter(hidden = true)
    InputStream body
  );

  @GetMapping
  @Operation(summary = "Get a user's avatar", description = "Served with the content type of the stored image, nosniff, and marked private")
  @ApiResponse(responseCode = "200", description = "The picture", content = {@Content(mediaType = "image/jpeg"), @Content(mediaType = "image/png")})
  @ApiResponse(responseCode = "404", description = "No user with this id, or the user has no avatar")
  ResponseEntity<byte[]> get(@PathVariable Long id);

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Remove a user's avatar, if they have one")
  @ApiResponse(responseCode = "204", description = "The user has no avatar")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  void remove(@PathVariable Long id);
}
