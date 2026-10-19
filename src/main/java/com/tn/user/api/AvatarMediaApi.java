package com.tn.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tn.user.domain.User;

@RequestMapping("/v1/users/{id}/avatar-media")
@Tag(name = "Avatar media", description = "A user's avatar as an item in tn-media-service, which holds the picture: the user holds only its id")
public interface AvatarMediaApi
{
  @PutMapping("/{mediaId}")
  @Operation(summary = "Point a user's avatar at a media item, replacing any it had", description = "The item is not checked and the old one is not deleted: both are the caller's to do")
  @ApiResponse(responseCode = "200", description = "The user, with its new avatarMediaId")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  User set(@PathVariable Long id, @PathVariable Long mediaId);

  @DeleteMapping
  @Operation(summary = "Record that a user has no avatar in tn-media-service (idempotent); the item is not deleted")
  @ApiResponse(responseCode = "200", description = "The user, with no avatarMediaId")
  @ApiResponse(responseCode = "404", description = "No user with this id")
  User remove(@PathVariable Long id);
}
