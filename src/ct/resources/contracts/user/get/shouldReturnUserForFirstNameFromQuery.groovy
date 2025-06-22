import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should return OK 200 and user for id"

  request
  {
    method GET()
    url("/?q=firstName=One||firstName=Two")
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
      {"id":1,"email":"one.first@mail.com","firstName":"One","lastName":"First","tokenSubject":"T1","created":"2025-06-20T17:31:01"},
      {"id":2,"email":"two.second@mail.com","firstName":"Two","lastName":"Second","tokenSubject":"T2","created":"2025-06-20T17:32:02"}
    ]
    """
    )
  }
}