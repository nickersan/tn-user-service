package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldReturnBadRequestForAvatarOverTheLimit implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should refuse an avatar that is larger than 100 KB");
      c.request(r ->
      {
        r.method(r.PUT());
        r.url("/v1/users/2/avatar");
        r.headers(h -> h.contentType("image/png"));
        r.body("avatar bytes");
      });
      c.response(r ->
      {
        r.status(r.BAD_REQUEST());
      });
    }));
  }
}
