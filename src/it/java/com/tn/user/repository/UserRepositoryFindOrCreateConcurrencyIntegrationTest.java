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
import com.tn.user.domain.Phone;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserRepositoryFindOrCreateConcurrencyIntegrationTest extends AbstractPostgresIntegrationTest
{
  private static final int CONCURRENT_CALLS = 16;

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldCreateExactlyOneProfileForConcurrentFindOrCreateCallsOnANewEmail() throws Exception
  {
    String email = "concurrent.test@testing.com";

    Set<Long> resultingIds = concurrently(() -> userRepository.findOrCreateByEmail(email).id());

    assertEquals(1, resultingIds.size());
    assertEquals(1, count(userRepository.findWhere("email = " + email)));
  }

  @Test
  void shouldCreateExactlyOneProfileForConcurrentFindOrCreateCallsOnANewPhone() throws Exception
  {
    Phone phone = new Phone(44, "7911123789");

    Set<Long> resultingIds = concurrently(() -> userRepository.findOrCreateByPhone(phone).id());

    assertEquals(1, resultingIds.size());
  }

  private Set<Long> concurrently(Callable<Long> call) throws Exception
  {
    ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_CALLS);
    try
    {
      List<Callable<Long>> calls = Stream.generate(() -> call)
        .limit(CONCURRENT_CALLS)
        .toList();

      return executorService.invokeAll(calls).stream()
        .map(this::get)
        .collect(Collectors.toSet());
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

  private <T> T get(Future<T> future)
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
