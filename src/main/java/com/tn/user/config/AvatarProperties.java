package com.tn.user.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param maxBytes             the most an upload, and the picture stored from it, may be
 * @param maxDimension         the most pixels either side of a picture may be
 * @param uploadLimit          how many uploads a user may start in an upload window
 * @param uploadWindow         the period {@code uploadLimit} applies over
 * @param maxConcurrentDecodes how many pictures may be decoded at once, since a large one takes megabytes to decode
 * @param decodeWait           how long an upload waits for its turn to decode before being refused
 */
@ConfigurationProperties("tn.user-service.avatar")
public record AvatarProperties(
  @DefaultValue("102400") int maxBytes,
  @DefaultValue("2048") int maxDimension,
  @DefaultValue("10") int uploadLimit,
  @DefaultValue("1h") Duration uploadWindow,
  @DefaultValue("4") int maxConcurrentDecodes,
  @DefaultValue("2s") Duration decodeWait
)
{
}
