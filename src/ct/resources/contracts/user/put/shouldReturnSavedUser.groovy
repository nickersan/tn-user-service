import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should save user and return OK 200"

  request
  {
    method PUT()
    url("/")
    headers
    {
      contentType("application/json")
    }
    body(
      """
      {"email":"one.first@mail.com","fullName":"One First","preferredName":"One","tokenSubject":"T1"}
      """
    )
  }

  response
  {
    status 200
    headers
    {
      contentType("application/json")
    }
    body(
      """
      {"id":1,"email":"one.first@mail.com","fullName":"One First","preferredName":"One","tokenSubject":"T1","created":"2025-06-20T17:31:01"}            
      """
    )
  }
}