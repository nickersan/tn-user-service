package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldUpdateUserWithPhone implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should replace a user's profile, adding a phone number as a country code and a national number");
      c.request(r ->
      {
        r.method(r.PUT());
        r.url("/v1/users/1");
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map()
          .entry("email", "test@testing.com")
          .entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7911123456"))
          .entry("fullName", "Test Tester")
          .entry("preferredName", "Test")
        );
      });
      c.response(r ->
      {
        r.status(r.OK());
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map()
          .entry("id", 1)
          .entry("email", "test@testing.com")
          .entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7911123456"))
          .entry("fullName", "Test Tester")
          .entry("preferredName", "Test")
        );
      });
    }));
  }
}
