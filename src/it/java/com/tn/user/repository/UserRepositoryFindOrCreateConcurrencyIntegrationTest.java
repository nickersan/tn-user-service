package com.tn.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tn.user.AbstractPostgresIntegrationTest;
import com.tn.user.domain.IdentifierType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserRepositoryFindOrCreateConcurrencyIntegrationTest extends AbstractPostgresIntegrationTest
{
  private static final int CONCURRENT_CALLS = 16;

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldCreateExactlyOneProfileForConcurrentFindOrCreateCallsOnANewIdentifier() throws Exception
  {
    String identifierValue = "concurrent.test@testing.com";

    ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_CALLS);
    try
    {
      List<Callable<Long>> calls = Stream.<Callable<Long>>generate(() -> () -> userRepository.findOrCreate(IdentifierType.EMAIL, identifierValue).id())
        .limit(CONCURRENT_CALLS)
        .toList();

      List<Future<Long>> futures = executorService.invokeAll(calls);

      Set<Long> resultingIds = futures.stream()
        .map(this::get)
        .collect(Collectors.toSet());

      assertEquals(1, resultingIds.size());
      assertEquals(1, count(userRepository.findWhere("identifierValue = " + identifierValue)));
    }
    finally
    {
      executorService.shutdown();
    }
  }

  private long count(Iterable<?> iterable)
  {
    return StreamSupport.stream(iterable.spliterator(), false).count();
  }

  private Long get(Future<Long> future)
  {
    try
    {
      return future.get();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
}
