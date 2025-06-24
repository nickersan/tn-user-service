import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save user without email and return BAD REQUEST 400"

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
      {"fullName":"One First","preferredName":"One","tokenSubject":"T1"}
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