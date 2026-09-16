package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldReturnBadRequestForUserWithoutIdentifier implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should return bad request for a user without an identifier");
      c.request(r ->
      {
        r.method(r.POST());
        r.url("/v1/users");
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map().entry("fullName", "No Identifier"));
      });
      c.response(r -> r.status(r.BAD_REQUEST()));
    }));
  }
}
