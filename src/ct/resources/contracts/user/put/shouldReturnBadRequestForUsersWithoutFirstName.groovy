import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should not save users without firstName and return BAD REQUEST 400"

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
        {"email":"one.first@mail.com","firstName":"One","lastName":"First","tokenSubject":"T1"},
        {"email":"two.second@mail.com","lastName":"Second","tokenSubject":"T2"},
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
      {"message":"Invalid body","detail":["firstName must not be null"]}                  
      """
    )
  }
}