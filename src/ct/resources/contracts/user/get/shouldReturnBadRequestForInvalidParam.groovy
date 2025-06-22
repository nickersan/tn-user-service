import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should return NOT FOUND 404"

  request
  {
    method GET()
    url("/?invalid=x")
  }

  response
  {
    status 400
  }
}