package object core {
  import cats.data.XorT
  import exceptions.UnexpectedState
  import scala.concurrent.Future

  type ServiceResponse[A] = XorT[Future, UnexpectedState, A]

}
