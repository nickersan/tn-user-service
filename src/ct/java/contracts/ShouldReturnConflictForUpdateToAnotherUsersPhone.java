package contracts;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

public class ShouldReturnConflictForUpdateToAnotherUsersPhone implements Supplier<Collection<Contract>>
{
  @Override
  public Collection<Contract> get()
  {
    return Collections.singletonList(Contract.make(c ->
    {
      c.description("should return conflict when updating a user to a phone number that belongs to another user");
      c.request(r ->
      {
        r.method(r.PUT());
        r.url("/v1/users/1");
        r.headers(h -> h.contentType(h.applicationJson()));
        r.body(ContractVerifierUtil.map()
          .entry("email", "test@testing.com")
          .entry("phone", ContractVerifierUtil.map().entry("countryCode", 44).entry("number", "7400123456"))
          .entry("fullName", "Test Tester")
          .entry("preferredName", "Test")
        );
      });
      c.response(r -> r.status(r.CONFLICT()));
    }));
  }
}
