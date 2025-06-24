import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should save users and return OK 200"

  request
  {
    method POST()
    url("/")
    headers
    {
      contentType("application/json")
    }
    body(
      """
      [
        {"email":"one.first@mail.com","fullName":"One First","preferredName":"One","tokenSubject":"T1"},
        {"email":"two.second@mail.com","fullName":"Two Second","preferredName":"Two","tokenSubject":"T2"},
        {"email":"three.third@mail.com","fullName":"Three Third","preferredName":"Three","tokenSubject":"T3"}
      ]
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
      [
        {"id":1,"email":"one.first@mail.com","fullName":"One First","preferredName":"One","tokenSubject":"T1","created":"2025-06-20T17:31:01"},
        {"id":2,"email":"two.second@mail.com","fullName":"Two Second","preferredName":"Two","tokenSubject":"T2","created":"2025-06-20T17:32:02"},
        {"id":3,"email":"three.third@mail.com","fullName":"Three Third","preferredName":"Three","tokenSubject":"T3","created":"2025-06-20T17:33:03"}
      ]            
      """
    )
  }
}