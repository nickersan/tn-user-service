package com.tn.user.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.stereotype.Component;

import com.tn.user.api.InvalidAvatarException;
import com.tn.user.api.TooManyAvatarUploadsException;
import com.tn.user.config.AvatarProperties;
import com.tn.user.domain.Avatar;

/**
 * Turns an upload into the avatar that is stored: a JPEG or PNG written afresh from the pixels alone, so that nothing
 * from the photo's embedded metadata (its location, camera, time taken, colour profile) is kept.
 *
 * <p>A file is judged by its contents, never by the type it was sent as. Its size in pixels is read from its header and
 * checked before it is decoded, since a small file can claim to be enormous. Decoding is done with the photo's metadata
 * left unread, and at most {@link AvatarProperties#maxConcurrentDecodes()} at a time.
 */
@Component
public class AvatarImages
{
  private static final String JPEG_FORMAT = "jpeg";
  private static final String PNG_FORMAT = "png";
  private static final String JPEG = "image/jpeg";
  private static final String PNG = "image/png";
  private static final float JPEG_QUALITY = 0.9f;

  static
  {
    // decode and encode in memory, never through a file in the temporary directory
    ImageIO.setUseCache(false);
  }

  private final AvatarProperties properties;
  private final Semaphore decodes;

  public AvatarImages(AvatarProperties properties)
  {
    this.properties = properties;
    this.decodes = new Semaphore(properties.maxConcurrentDecodes());
  }

  /**
   * @throws InvalidAvatarException           if the upload is too large, isn't a JPEG or PNG, or is too large in pixels
   * @throws TooManyAvatarUploadsException    if it is not decoded because as many as are allowed already are
   */
  public Avatar process(byte[] upload)
  {
    if (upload.length == 0) throw new InvalidAvatarException("The avatar is empty");
    if (upload.length > properties.maxBytes()) throw new InvalidAvatarException("The avatar is larger than " + properties.maxBytes() + " bytes");

    try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(upload)))
    {
      ImageReader reader = readerFor(input);

      try
      {
        // ignore the metadata: it isn't kept, and the parsers of it are where image libraries have had their bugs
        reader.setInput(input, true, true);
        String contentType = JPEG_FORMAT.equals(format(reader)) ? JPEG : PNG;

        checkDimensions(reader);

        BufferedImage image = decode(reader);
        Avatar avatar = new Avatar(contentType, encode(image, contentType));

        if (avatar.image().length > properties.maxBytes()) throw new InvalidAvatarException("The avatar is larger than " + properties.maxBytes() + " bytes once re-encoded");

        return avatar;
      }
      finally
      {
        reader.dispose();
      }
    }
    catch (IOException | RuntimeException e)
    {
      if (e instanceof InvalidAvatarException invalid) throw invalid;
      if (e instanceof TooManyAvatarUploadsException busy) throw busy;

      throw new InvalidAvatarException("The avatar can't be read as a JPEG or PNG", e);
    }
  }

  private static ImageReader readerFor(ImageInputStream input)
  {
    if (input == null) throw new InvalidAvatarException("The avatar is not a JPEG or PNG");

    Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

    while (readers.hasNext())
    {
      ImageReader reader = readers.next();
      String format = format(reader);

      if (JPEG_FORMAT.equals(format) || PNG_FORMAT.equals(format)) return reader;

      reader.dispose();
    }

    throw new InvalidAvatarException("The avatar is not a JPEG or PNG");
  }

  private static String format(ImageReader reader)
  {
    try
    {
      return reader.getFormatName().toLowerCase();
    }
    catch (IOException e)
    {
      throw new InvalidAvatarException("The avatar can't be read as a JPEG or PNG", e);
    }
  }

  private void checkDimensions(ImageReader reader) throws IOException
  {
    int width = reader.getWidth(0);
    int height = reader.getHeight(0);
    int max = properties.maxDimension();

    if (width < 1 || height < 1 || width > max || height > max)
    {
      throw new InvalidAvatarException("The avatar is " + width + " by " + height + " pixels; it can be at most " + max + " on a side");
    }
  }

  private BufferedImage decode(ImageReader reader) throws IOException
  {
    try
    {
      if (!decodes.tryAcquire(properties.decodeWait().toMillis(), TimeUnit.MILLISECONDS))
      {
        throw new TooManyAvatarUploadsException("Too many avatars are being processed; try again shortly");
      }
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      throw new TooManyAvatarUploadsException("Interrupted while waiting to process the avatar");
    }

    try
    {
      return reader.read(0);
    }
    finally
    {
      decodes.release();
    }
  }

  /** Draws the pixels onto a plain sRGB image, which drops any colour profile as well, and writes that. */
  private static byte[] encode(BufferedImage decoded, String contentType) throws IOException
  {
    boolean jpeg = JPEG.equals(contentType);
    boolean alpha = !jpeg && decoded.getColorModel().hasAlpha();
    BufferedImage pixels = new BufferedImage(decoded.getWidth(), decoded.getHeight(), alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = pixels.createGraphics();

    try
    {
      graphics.drawImage(decoded, 0, 0, null);
    }
    finally
    {
      graphics.dispose();
    }

    ImageWriter writer = ImageIO.getImageWritersByFormatName(jpeg ? JPEG_FORMAT : PNG_FORMAT).next();

    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ImageOutputStream output = ImageIO.createImageOutputStream(bytes))
    {
      ImageWriteParam parameters = writer.getDefaultWriteParam();

      if (jpeg)
      {
        parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parameters.setCompressionQuality(JPEG_QUALITY);
      }

      writer.setOutput(output);
      // no metadata is passed, so the writer adds only what the format needs
      writer.write(null, new IIOImage(pixels, null, null), parameters);
      output.flush();

      return bytes.toByteArray();
    }
    finally
    {
      writer.dispose();
    }
  }
}
