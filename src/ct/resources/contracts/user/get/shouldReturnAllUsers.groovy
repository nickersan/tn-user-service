import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should return OK 200 and all users"

  request
  {
    method GET()
    url("/")
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
        {"id":2,"email":"two.second@mail.com","firstName":"Two","lastName":"Second","tokenSubject":"T2","created":"2025-06-20T17:32:02"},
        {"id":3,"email":"three.third@mail.com","firstName":"Three","lastName":"Third","tokenSubject":"T3","created":"2025-06-20T17:33:03"}
      ]      
      """
    )
  }
}