package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldClearAvatarMedia implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should record that a user has no avatar in the media service");
      c.request(r ->
      {
        r.method(r.DELETE());
        r.url("/v1/users/8/avatar-media");
      });
      c.response(r ->
      {
        r.status(r.OK());
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map()
          .entry("id", 8)
          .entry("email", "nomedia@testing.com")
        );
      });
    }));
  }
}
