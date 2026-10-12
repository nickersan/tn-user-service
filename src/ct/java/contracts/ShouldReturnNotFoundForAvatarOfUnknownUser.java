package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;

public class ShouldReturnNotFoundForAvatarOfUnknownUser implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should return not found for the avatar of an unknown user id");
      c.request(r ->
      {
        r.method(r.GET());
        r.url("/v1/users/999/avatar");
      });
      c.response(r -> r.status(r.NOT_FOUND()));
    }));
  }
}
