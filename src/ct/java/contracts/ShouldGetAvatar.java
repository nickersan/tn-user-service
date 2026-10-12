package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;

public class ShouldGetAvatar implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should get a user's avatar with the type of the stored image, nosniff, and private");
      c.request(r ->
      {
        r.method(r.GET());
        r.url("/v1/users/1/avatar");
      });
      c.response(r ->
      {
        r.status(r.OK());
        r.headers(h ->
        {
          h.contentType("image/png");
          h.header("X-Content-Type-Options", "nosniff");
          h.header("Cache-Control", "no-cache, private");
        });
      });
    }));
  }
}
