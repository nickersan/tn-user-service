package com.tn.user.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.tn.user.domain.IdentifierType.EMAIL;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.mask.MaskingJsonGeneratorDecorator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.tn.user.api.UserActionsApi.FindOrCreateRequest;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

class UserActionsControllerLoggingTest
{
  private static final String EMAIL_VALUE = "test@testing.com";

  private Logger logger;
  private ListAppender<ILoggingEvent> appender;
  private LogstashEncoder encoder;

  @BeforeEach
  void setup()
  {
    logger = (Logger)LoggerFactory.getLogger(UserActionsController.class);

    appender = new ListAppender<>();
    appender.setContext(logger.getLoggerContext());
    appender.start();
    logger.addAppender(appender);

    MaskingJsonGeneratorDecorator decorator = new MaskingJsonGeneratorDecorator();
    decorator.setDefaultMask("****");
    decorator.addPath("email");
    decorator.addPath("phone");
    decorator.start();

    encoder = new LogstashEncoder();
    encoder.setContext(logger.getLoggerContext());
    encoder.addDecorator(decorator);
    encoder.start();
  }

  @AfterEach
  void teardown()
  {
    logger.detachAppender(appender);
  }

  @Test
  void shouldLogFindOrCreateWithoutLeakingTheIdentifierValue()
  {
    UserRepository userRepository = mock(UserRepository.class);
    when(userRepository.findOrCreate(EMAIL, EMAIL_VALUE)).thenReturn(new User(1L, EMAIL_VALUE, null, null, null, null));

    new UserActionsController(userRepository).findOrCreate(new FindOrCreateRequest(EMAIL, EMAIL_VALUE));

    List<ILoggingEvent> events = appender.list;
    assertTrue(events.stream().anyMatch(event -> event.getLevel() == Level.INFO && event.getFormattedMessage().equals("Find-or-create resolved")));

    for (ILoggingEvent event : events)
    {
      String json = new String(encoder.encode(event));
      assertFalse(json.contains(EMAIL_VALUE), "log entry must not contain the unmasked identifier value");
    }
  }
}
