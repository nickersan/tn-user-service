package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldFindOrCreateUserByPhone implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should find or create a user for a phone number");
      c.request(r ->
      {
        r.method(r.POST());
        r.url("/v1/actions/find-or-create");
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map().entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7911123456")));
      });
      c.response(r ->
      {
        r.status(r.OK());
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map().entry("id", 3).entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7911123456")));
      });
    }));
  }
}
