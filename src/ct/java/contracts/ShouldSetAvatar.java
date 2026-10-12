package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldSetAvatar implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should set a user's avatar and return the user with its version");
      c.request(r ->
      {
        r.method(r.PUT());
        r.url("/v1/users/1/avatar");
        r.headers(h -> h.contentType("image/png"));
        r.body("avatar bytes");
      });
      c.response(r ->
      {
        r.status(r.OK());
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map()
          .entry("id", 1)
          .entry("email", "test@testing.com")
          .entry("fullName", "Test Tester")
          .entry("avatarVersion", 1)
        );
      });
    }));
  }
}
