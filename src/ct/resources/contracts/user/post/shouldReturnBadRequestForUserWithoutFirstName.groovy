import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save user without firstName and return BAD REQUEST 400"

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
      {"email":"one.first@mail.com","lastName":"First","tokenSubject":"T1"}
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
      {"message":"Invalid body","detail":["firstName must not be null"]}            
      """
    )
  }
}