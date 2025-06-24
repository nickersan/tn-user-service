import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should return OK 200 and user for id"

  request
  {
    method GET()
    url("/1")
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