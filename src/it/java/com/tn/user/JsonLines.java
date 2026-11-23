package com.tn.user;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The lines the service logs, as they are written: encoded by the encoder of the {@code STDOUT} appender its {@code logback-spring.xml}
 * configures. A line that is not JSON, or that has a key twice (a service's own field clashing with a correlation field, say), is
 * rejected: the next look at the lines fails with it.
 */
public class JsonLines extends AppenderBase<ILoggingEvent>
{
  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
  private static final String APPENDER = "STDOUT";
  private static final String REQUEST_LOGGER = "com.tn.service.logging.RequestLoggingFilter";
  private static final long WAIT_MILLIS = 5_000;
  private static final long POLL_MILLIS = 10;

  private final List<JsonNode> lines = new CopyOnWriteArrayList<>();
  private final List<String> rejected = new CopyOnWriteArrayList<>();
  private final Encoder<ILoggingEvent> encoder;

  private JsonLines(Encoder<ILoggingEvent> encoder)
  {
    this.encoder = encoder;
  }

  public static JsonLines capture()
  {
    LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    if (!(root.getAppender(APPENDER) instanceof OutputStreamAppender<ILoggingEvent> stdout))
    {
      throw new IllegalStateException("logback-spring.xml has no " + APPENDER + " appender");
    }

    JsonLines jsonLines = new JsonLines(stdout.getEncoder());
    jsonLines.setContext(loggerContext);
    jsonLines.start();
    root.addAppender(jsonLines);

    return jsonLines;
  }

  public void release()
  {
    ((LoggerContext)LoggerFactory.getILoggerFactory()).getLogger(Logger.ROOT_LOGGER_NAME).detachAppender(this);
    stop();
  }

  /**
   * @return the lines from the logger named, in the order they were written
   */
  public List<JsonNode> from(String loggerName)
  {
    if (!rejected.isEmpty()) throw new IllegalStateException("Lines rejected: " + rejected);

    return lines.stream().filter(line -> loggerName.equals(line.path("logger_name").asString())).toList();
  }

  /**
   * The one line each request answered is logged with (tn-service), in the order they were written. A request's line is written once it
   * has been answered, so possibly just after its caller has the answer: this waits a little for the number expected.
   */
  public List<JsonNode> requests(int expected) throws InterruptedException
  {
    long until = System.currentTimeMillis() + WAIT_MILLIS;
    while (from(REQUEST_LOGGER).size() < expected && System.currentTimeMillis() < until) Thread.sleep(POLL_MILLIS);

    return from(REQUEST_LOGGER);
  }

  @Override
  protected void append(ILoggingEvent event)
  {
    String line = new String(encoder.encode(event), StandardCharsets.UTF_8);
    try
    {
      lines.add(JSON_MAPPER.readTree(line));
    }
    catch (JacksonException e)
    {
      rejected.add(e.getOriginalMessage() + ": " + line.strip());
    }
  }
}
