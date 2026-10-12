package com.tn.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.tn.user.api.InvalidAvatarException;
import com.tn.user.config.AvatarProperties;
import com.tn.user.domain.Avatar;
import com.tn.user.service.AvatarService;

class AvatarControllerTest
{
  private static final int MAX_BYTES = 102_400;

  private final AvatarService avatarService = mock(AvatarService.class);
  private final AvatarController controller = new AvatarController(avatarService, new AvatarProperties(MAX_BYTES, 2048, 10, Duration.ofHours(1), 4, Duration.ofSeconds(2)));

  @Test
  void anOversizedBodyIsNotReadInFull()
  {
    AtomicLong read = new AtomicLong();

    assertThrows(InvalidAvatarException.class, () -> controller.set(1L, null, endless(read)));

    // 50 MB or more is on offer; what was taken is the limit, and the one byte that shows it was passed
    assertTrue(read.get() <= MAX_BYTES + 1, "read " + read.get() + " bytes");
    verifyNoInteractions(avatarService);
  }

  @Test
  void aBodyDeclaredOversizeIsNotReadAtAll()
  {
    AtomicLong read = new AtomicLong();

    assertThrows(InvalidAvatarException.class, () -> controller.set(1L, 50L * 1024 * 1024, endless(read)));

    assertEquals(0, read.get());
    verifyNoInteractions(avatarService);
  }

  @Test
  void anAvatarIsServedWithTheTypeOfTheStoredImageAndKeptPrivate()
  {
    when(avatarService.get(1L)).thenReturn(new Avatar("image/png", new byte[] {1, 2, 3}));

    ResponseEntity<byte[]> response = controller.get(1L);

    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
    String cacheControl = response.getHeaders().getCacheControl();
    assertTrue(cacheControl.contains("private"), cacheControl);
    assertTrue(!cacheControl.contains("public"), cacheControl);
  }

  private static InputStream endless(AtomicLong read)
  {
    return new InputStream()
    {
      @Override
      public int read()
      {
        read.incrementAndGet();

        return 0;
      }

      @Override
      public int read(byte[] bytes, int offset, int length) throws IOException
      {
        read.addAndGet(length);

        return length;
      }
    };
  }
}
