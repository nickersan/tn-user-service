import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save users without email and return BAD REQUEST 400"

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
      [
        {"email":"one.first@mail.com","fullName":"One First","preferredName":"One","tokenSubject":"T1"},
        {"fullName":"Two Second","preferredName":"Two","tokenSubject":"T2"},
        {"email":"three.third@mail.com","fullName":"Three Third","preferredName":"Three","tokenSubject":"T3"}
      ]
      """
    )
  }

  response
  {
    status 400
    headers
    {
      contentType("application/json")
    }
    body(
      """
      {"message":"Invalid body","detail":["email must not be null"]}                  
      """
    )
  }
}