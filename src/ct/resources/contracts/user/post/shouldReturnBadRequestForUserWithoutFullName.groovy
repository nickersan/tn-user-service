import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save user without fullName and return BAD REQUEST 400"

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
      {"email":"one.first@mail.com","preferredName":"One","tokenSubject":"T1"}
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