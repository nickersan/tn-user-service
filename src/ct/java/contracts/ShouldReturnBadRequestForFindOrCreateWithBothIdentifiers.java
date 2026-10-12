package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldReturnBadRequestForFindOrCreateWithBothIdentifiers implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should return bad request for find-or-create with both an email and a phone number");
      c.request(r ->
      {
        r.method(r.POST());
        r.url("/v1/actions/find-or-create");
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map().entry("email", "new@testing.com").entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7911123456")));
      });
      c.response(r ->
      {
        r.status(r.BAD_REQUEST());
      });
    }));
  }
}
