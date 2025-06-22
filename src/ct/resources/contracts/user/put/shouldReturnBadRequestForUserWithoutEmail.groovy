import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save user without email and return BAD REQUEST 400"

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
      {"firstName":"One","lastName":"First","tokenSubject":"T1"}
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