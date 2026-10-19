package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;

public class ShouldReturnNotFoundForAvatarMediaOfUnknownUser implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should answer not found when pointing the avatar of a user who does not exist");
      c.request(r ->
      {
        r.method(r.PUT());
        r.url("/v1/users/999/avatar-media/123");
      });
      c.response(r ->
      {
        r.status(r.NOT_FOUND());
      });
    }));
  }
}
