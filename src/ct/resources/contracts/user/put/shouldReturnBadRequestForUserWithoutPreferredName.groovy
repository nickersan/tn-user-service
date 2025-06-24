import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save user without preferredName and return BAD REQUEST 400"

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
      {"email":"one.first@mail.com","fullName":"One First","tokenSubject":"T1"}
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
      {"message":"Invalid body","detail":["preferredName must not be null"]}                        
      """
    )
  }
}