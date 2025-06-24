import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save users without fullName and return BAD REQUEST 400"

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
        {"email":"two.second@mail.com","preferredName":"Two","tokenSubject":"T2"},
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
      {"message":"Invalid body","detail":["fullName must not be null"]}                  
      """
    )
  }
}