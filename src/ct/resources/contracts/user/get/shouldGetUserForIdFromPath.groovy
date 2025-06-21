import org.springframework.cloud.contract.spec.Contract

Contract.make
  {
    description "should return OK 200 and all users"

    request
      {
        method GET()
        url("/1")
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
          {"id":1,"email":"one.first@mail.com","firstName":"One","lastName":"First","tokenSubject":"T1","created":"2025-06-20T17:31:01"}
          """
        )
      }
  }