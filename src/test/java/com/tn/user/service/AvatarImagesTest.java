package com.tn.user.service;

import static com.tn.user.service.AvatarFixtures.contains;
import static com.tn.user.service.AvatarFixtures.decode;
import static com.tn.user.service.AvatarFixtures.jpegMarkers;
import static com.tn.user.service.AvatarFixtures.pngChunks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tn.user.api.InvalidAvatarException;
import com.tn.user.api.TooManyAvatarUploadsException;
import com.tn.user.config.AvatarProperties;
import com.tn.user.domain.Avatar;

class AvatarImagesTest
{
  private static final int MAX_BYTES = 102_400;
  // markers a JPEG written without metadata may have: JFIF, the tables, the frame and the scan
  private static final List<Integer> ALLOWED_JPEG_MARKERS = List.of(0xE0, 0xDB, 0xC0, 0xC4, 0xDA);
  private static final List<String> ALLOWED_PNG_CHUNKS = List.of("IHDR", "PLTE", "tRNS", "IDAT", "IEND");

  private final AvatarImages images = new AvatarImages(properties(4));

  @Test
  void aJpegWithinTheLimitsIsAccepted()
  {
    Avatar avatar = images.process(AvatarFixtures.jpeg(128, 128));

    assertEquals("image/jpeg", avatar.contentType());
    assertEquals(128, decode(avatar.image()).getWidth());
  }

  @Test
  void aPngWithinTheLimitsIsAccepted()
  {
    Avatar avatar = images.process(AvatarFixtures.png(100, 80));

    assertEquals("image/png", avatar.contentType());
    assertEquals(80, decode(avatar.image()).getHeight());
  }

  @Test
  void aPictureOver100KbIsRefused()
  {
    assertThrows(InvalidAvatarException.class, () -> images.process(new byte[MAX_BYTES + 1]));
  }

  @Test
  void aFileThatIsNotAPictureIsRefusedWhateverItWasSentAs()
  {
    assertThrows(InvalidAvatarException.class, () -> images.process(AvatarFixtures.text()));
  }

  @Test
  void anEmptyFileIsRefused()
  {
    assertThrows(InvalidAvatarException.class, () -> images.process(new byte[0]));
  }

  @Test
  void aGifIsRefused()
  {
    assertThrows(InvalidAvatarException.class, () -> images.process(AvatarFixtures.gif()));
  }

  @Test
  void aPictureClaimingEnormousDimensionsIsRefusedWithoutDecodingIt()
  {
    // with no permit to decode, a picture that got as far as decoding would be refused as too many, not as invalid
    AvatarImages noDecodingPossible = new AvatarImages(properties(0));

    assertThrows(InvalidAvatarException.class, () -> noDecodingPossible.process(AvatarFixtures.pngClaiming(20_000, 20_000)));
  }

  @Test
  void aPictureOfExactlyTheLargestSizeIsAccepted()
  {
    assertEquals(2048, decode(images.process(AvatarFixtures.pngClaimingNothing(2048, 1)).image()).getWidth());
  }

  @Test
  void aPictureOnePixelOverTheLargestSizeIsRefused()
  {
    assertThrows(InvalidAvatarException.class, () -> images.process(AvatarFixtures.pngClaimingNothing(2049, 1)));
  }

  @Test
  void locationIsRemoved()
  {
    byte[] upload = AvatarFixtures.jpegWithGps(96, 64);
    assertTrue(contains(upload, "Exif"), "the fixture should carry EXIF data");

    Avatar avatar = images.process(upload);

    assertFalse(contains(avatar.image(), "Exif"));
    assertTrue(ALLOWED_JPEG_MARKERS.containsAll(jpegMarkers(avatar.image())), "markers: " + jpegMarkers(avatar.image()));
    BufferedImage image = decode(avatar.image());
    assertEquals(96, image.getWidth());
    assertEquals(64, image.getHeight());
  }

  @Test
  void textChunksAreRemovedFromAPng()
  {
    byte[] upload = AvatarFixtures.pngWithText(64, 64);
    assertTrue(pngChunks(upload).contains("tEXt"), "the fixture should carry a text chunk");

    Avatar avatar = images.process(upload);

    assertTrue(ALLOWED_PNG_CHUNKS.containsAll(pngChunks(avatar.image())), "chunks: " + pngChunks(avatar.image()));
  }

  @Test
  void aMalformedColourProfileIsDroppedNotParsed()
  {
    Avatar avatar = images.process(AvatarFixtures.jpegWithMalformedColourProfile(64, 64));

    assertFalse(contains(avatar.image(), "ICC_PROFILE"));
    assertEquals(64, decode(avatar.image()).getWidth());
  }

  @Test
  void aPictureOverTheLimitOnceReEncodedIsRefused()
  {
    byte[] upload = AvatarFixtures.indexedPngThatGrows();
    assertTrue(upload.length <= MAX_BYTES, "the fixture should be under the limit as sent: " + upload.length);

    assertThrows(InvalidAvatarException.class, () -> images.process(upload));
  }

  @Test
  void pictureIsRefusedWhenAsManyAreBeingDecodedAsAreAllowed()
  {
    AvatarImages busy = new AvatarImages(properties(0));

    assertThrows(TooManyAvatarUploadsException.class, () -> busy.process(AvatarFixtures.jpeg(32, 32)));
  }

  private static AvatarProperties properties(int maxConcurrentDecodes)
  {
    return new AvatarProperties(MAX_BYTES, 2048, 10, Duration.ofHours(1), maxConcurrentDecodes, Duration.ofMillis(50));
  }
}
