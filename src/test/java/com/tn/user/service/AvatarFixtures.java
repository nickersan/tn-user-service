package com.tn.user.service;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

/** Pictures, and hostile files made to look like them, for the avatar tests. */
public final class AvatarFixtures
{
  private static final byte[] PNG_SIGNATURE = {(byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};

  private AvatarFixtures()
  {
  }

  /** A JPEG that is noise, so that its size is not far from what its pixels can compress to. */
  public static byte[] jpeg(int width, int height)
  {
    return encode(noise(width, height, BufferedImage.TYPE_INT_RGB, 1), "jpeg");
  }

  public static byte[] png(int width, int height)
  {
    return encode(noise(width, height, BufferedImage.TYPE_INT_RGB, 2), "png");
  }

  /** A JPEG with an EXIF segment holding GPS coordinates' reference: what a phone's camera adds. */
  public static byte[] jpegWithGps(int width, int height)
  {
    return insertAfterSoi(jpeg(width, height), segment(0xE1, exif()));
  }

  /** A JPEG with a colour profile segment that is not a colour profile. */
  public static byte[] jpegWithMalformedColourProfile(int width, int height)
  {
    byte[] profile = new byte[100];
    new Random(3).nextBytes(profile);

    ByteBuffer data = ByteBuffer.allocate(14 + profile.length);
    data.put("ICC_PROFILE\0".getBytes(StandardCharsets.US_ASCII)).put((byte)1).put((byte)1).put(profile);

    return insertAfterSoi(jpeg(width, height), segment(0xE2, data.array()));
  }

  /** A PNG with text chunks, as an editor or camera app leaves. */
  public static byte[] pngWithText(int width, int height)
  {
    byte[] png = png(width, height);
    int afterHeader = PNG_SIGNATURE.length + 12 + 13;
    byte[] text = chunk("tEXt", "Comment\0taken at 51.5,-0.12".getBytes(StandardCharsets.ISO_8859_1));

    return splice(png, afterHeader, text);
  }

  /** An under-100 KB PNG whose header says it is the given size, with nothing like that many pixels behind it. */
  public static byte[] pngClaiming(int width, int height)
  {
    ByteBuffer header = ByteBuffer.allocate(13).putInt(width).putInt(height).put((byte)8).put((byte)2).put((byte)0).put((byte)0).put((byte)0);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    bytes.writeBytes(PNG_SIGNATURE);
    bytes.writeBytes(chunk("IHDR", header.array()));
    bytes.writeBytes(chunk("IEND", new byte[0]));

    return bytes.toByteArray();
  }

  /**
   * A PNG of 256 colours that is under 100 KB, but is over it once written as the full-colour image that is stored.
   * Its pixels are random, so there is nothing for the larger form to compress away.
   */
  public static byte[] indexedPngThatGrows()
  {
    byte[] reds = new byte[256];
    byte[] greens = new byte[256];
    byte[] blues = new byte[256];
    Random random = new Random(4);
    random.nextBytes(reds);
    random.nextBytes(greens);
    random.nextBytes(blues);

    BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(8, 256, reds, greens, blues));
    for (int y = 0; y < 300; y++)
    {
      for (int x = 0; x < 300; x++) image.getRaster().setSample(x, y, 0, random.nextInt(256));
    }

    return encode(image, "png");
  }

  /** A real PNG of the given size, all one colour so that it is small whatever its size. */
  public static byte[] pngClaimingNothing(int width, int height)
  {
    return encode(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png");
  }

  public static byte[] text()
  {
    return "this is not a picture".getBytes(StandardCharsets.UTF_8);
  }

  public static byte[] gif()
  {
    return encode(noise(32, 32, BufferedImage.TYPE_INT_RGB, 5), "gif");
  }

  /** The markers of a JPEG's segments, in order, up to the start of the image data. */
  public static List<Integer> jpegMarkers(byte[] jpeg)
  {
    List<Integer> markers = new ArrayList<>();
    int at = 2;

    while (at + 4 <= jpeg.length)
    {
      int marker = jpeg[at + 1] & 0xFF;
      markers.add(marker);
      if (marker == 0xDA) break;
      at += 2 + (((jpeg[at + 2] & 0xFF) << 8) | (jpeg[at + 3] & 0xFF));
    }

    return markers;
  }

  /** The types of a PNG's chunks, in order. */
  public static List<String> pngChunks(byte[] png)
  {
    List<String> types = new ArrayList<>();
    ByteBuffer buffer = ByteBuffer.wrap(png).position(PNG_SIGNATURE.length);

    while (buffer.remaining() >= 12)
    {
      int length = buffer.getInt();
      byte[] type = new byte[4];
      buffer.get(type);
      types.add(new String(type, StandardCharsets.US_ASCII));
      buffer.position(buffer.position() + length + 4);
    }

    return types;
  }

  public static boolean contains(byte[] bytes, String ascii)
  {
    return new String(bytes, StandardCharsets.ISO_8859_1).contains(ascii);
  }

  public static BufferedImage decode(byte[] image)
  {
    try
    {
      return ImageIO.read(new java.io.ByteArrayInputStream(image));
    }
    catch (IOException e)
    {
      throw new IllegalStateException(e);
    }
  }

  private static BufferedImage noise(int width, int height, int type, long seed)
  {
    BufferedImage image = new BufferedImage(width, height, type);
    Random random = new Random(seed);

    for (int y = 0; y < height; y++)
    {
      for (int x = 0; x < width; x++) image.setRGB(x, y, random.nextInt() & 0xFFFFFF);
    }

    return image;
  }

  private static byte[] encode(BufferedImage image, String format)
  {
    try
    {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ImageIO.write(image, format, bytes);

      return bytes.toByteArray();
    }
    catch (IOException e)
    {
      throw new IllegalStateException(e);
    }
  }

  /** An EXIF block: a TIFF holding a GPS directory whose only entry is a latitude reference. */
  private static byte[] exif()
  {
    ByteBuffer tiff = ByteBuffer.allocate(8 + 18 + 18);
    tiff.put("MM".getBytes(StandardCharsets.US_ASCII)).putShort((short)42).putInt(8);
    // IFD0: one entry, the pointer to the GPS directory at offset 26
    tiff.putShort((short)1).putShort((short)0x8825).putShort((short)4).putInt(1).putInt(26).putInt(0);
    // GPS directory: one entry, GPSLatitudeRef = "N"
    tiff.putShort((short)1).putShort((short)1).putShort((short)2).putInt(2).put((byte)'N').put((byte)0).put((byte)0).put((byte)0).putInt(0);

    ByteBuffer exif = ByteBuffer.allocate(6 + tiff.capacity());
    exif.put("Exif\0\0".getBytes(StandardCharsets.US_ASCII)).put(tiff.array());

    return exif.array();
  }

  private static byte[] segment(int marker, byte[] data)
  {
    return ByteBuffer.allocate(4 + data.length).put((byte)0xFF).put((byte)marker).putShort((short)(data.length + 2)).put(data).array();
  }

  private static byte[] insertAfterSoi(byte[] jpeg, byte[] segment)
  {
    return splice(jpeg, 2, segment);
  }

  private static byte[] splice(byte[] bytes, int at, byte[] insert)
  {
    ByteBuffer result = ByteBuffer.allocate(bytes.length + insert.length);
    result.put(bytes, 0, at).put(insert).put(bytes, at, bytes.length - at);

    return result.array();
  }

  private static byte[] chunk(String type, byte[] data)
  {
    byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
    CRC32 crc = new CRC32();
    crc.update(typeBytes);
    crc.update(data);

    return ByteBuffer.allocate(12 + data.length).putInt(data.length).put(typeBytes).put(data).putInt((int)crc.getValue()).array();
  }
}
