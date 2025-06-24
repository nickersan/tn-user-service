import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should return OK 200 and user for full-name"

  request
  {
    method GET()
    url("/?q=fullName=One First||fullName=Two Second")
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
    [
      {"id":1,"email":"one.first@mail.com","fullName":"One First","preferredName":"One","tokenSubject":"T1","created":"2025-06-20T17:31:01"},
      {"id":2,"email":"two.second@mail.com","fullName":"Two Second","preferredName":"Two","tokenSubject":"T2","created":"2025-06-20T17:32:02"}
    ]
    """
    )
  }
}