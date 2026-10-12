package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldCreateUserWithPhone implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should create a user with a phone number as a country code and a national number");
      c.request(r ->
      {
        r.method(r.POST());
        r.url("/v1/users");
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map().entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7911123456")));
      });
      c.response(r ->
      {
        r.status(201);
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map().entry("id", 3).entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7911123456")));
      });
    }));
  }
}
