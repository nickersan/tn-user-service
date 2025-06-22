import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save users without lastName and return BAD REQUEST 400"

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
        {"email":"one.first@mail.com","firstName":"One","lastName":"First","tokenSubject":"T1"},
        {"email":"two.second@mail.com","firstName":"Two","tokenSubject":"T2"},
        {"email":"three.third@mail.com","firstName":"Three","lastName":"Third","tokenSubject":"T3"}
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
      {"message":"Invalid body","detail":["lastName must not be null"]}                  
      """
    )
  }
}