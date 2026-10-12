package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;

public class ShouldDeleteUser implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should delete a user");
      c.request(r ->
      {
        r.method(r.DELETE());
        r.url("/v1/users/4");
      });
      c.response(r -> r.status(r.NO_CONTENT()));
    }));
  }
}
