package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldReplaceAvatar implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should replace a user's avatar, giving it a new version");
      c.request(r ->
      {
        r.method(r.PUT());
        r.url("/v1/users/5/avatar");
        r.headers(h -> h.contentType("image/png"));
        r.body("avatar bytes");
      });
      c.response(r ->
      {
        r.status(r.OK());
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map()
          .entry("id", 5)
          .entry("email", "replaced@testing.com")
          .entry("avatarVersion", 2)
        );
      });
    }));
  }
}
