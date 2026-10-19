package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldSetAvatarMedia implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should point a user avatar at a media item and return the user with its id");
      c.request(r ->
      {
        r.method(r.PUT());
        r.url("/v1/users/7/avatar-media/123");
      });
      c.response(r ->
      {
        r.status(r.OK());
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map()
          .entry("id", 7)
          .entry("email", "media@testing.com")
          .entry("avatarMediaId", 123)
        );
      });
    }));
  }
}
