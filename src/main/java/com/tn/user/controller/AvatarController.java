package com.tn.user.controller;

import java.io.IOException;
import java.io.InputStream;

import lombok.AllArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tn.user.api.AvatarApi;
import com.tn.user.api.InvalidAvatarException;
import com.tn.user.config.AvatarProperties;
import com.tn.user.domain.Avatar;
import com.tn.user.domain.User;
import com.tn.user.service.AvatarService;

@AllArgsConstructor
@RestController
public class AvatarController implements AvatarApi
{
  private final AvatarService avatarService;
  private final AvatarProperties properties;

  @Override
  public User set(Long id, Long contentLength, InputStream body)
  {
    int max = properties.maxBytes();

    if (contentLength != null && contentLength > max) throw tooLarge(max);

    return avatarService.set(id, read(body, max));
  }

  @Override
  public ResponseEntity<byte[]> get(Long id)
  {
    Avatar avatar = avatarService.get(id);

    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(avatar.contentType()))
      .header("X-Content-Type-Options", "nosniff")
      // private: a proxy or CDN in front of this must not keep a user's picture for anyone else
      .cacheControl(CacheControl.noCache().cachePrivate())
      .body(avatar.image());
  }

  @Override
  public void remove(Long id)
  {
    avatarService.remove(id);
  }

  /** Reads no more than one byte past the limit, so that an oversized body is never held in full. */
  private byte[] read(InputStream body, int max)
  {
    try
    {
      byte[] bytes = body.readNBytes(max + 1);

      if (bytes.length > max) throw tooLarge(max);

      return bytes;
    }
    catch (IOException e)
    {
      throw new InvalidAvatarException("The avatar could not be read", e);
    }
  }

  private static InvalidAvatarException tooLarge(int max)
  {
    return new InvalidAvatarException("The avatar is larger than " + max + " bytes");
  }
}
