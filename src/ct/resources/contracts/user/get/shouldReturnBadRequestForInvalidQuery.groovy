import org.springframework.cloud.contract.spec.Contract

Contract.make
{
  description "should return BAD REQUEST 400"

  request
  {
    method GET()
    url("/?q=invalid=x")
  }

  response
  {
    status 400
  }
}