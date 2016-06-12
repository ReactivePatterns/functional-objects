package functional.objects

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import API._
import DSL._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object Domain {
  // queries
  case class QueryValue() extends API.Query[Int]

  // commands
  trait StateCommand extends API.Command[Int]
  case class SetValue(newValue: Int) extends StateCommand
  case class IncrementValue(value: Int) extends StateCommand
  case class DecrementValue(value: Int) extends StateCommand
  case class BadCommand() extends API.Command[Int]
}

import Domain._

class ComponentTest(system: ActorSystem) extends TestKit(system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("auth-test", ConfigFactory.load("application.conf")))

  val processor: PartialFunction[(Int, StateCommand), Int] = {
    case (current: Int, command: SetValue) => command.newValue
    case (current: Int, command: IncrementValue) => current + command.value
    case (current: Int, command: DecrementValue) => current - command.value
  }

  class StatefulService extends SimpleComponent[Int, StateCommand](0, processor)

  val stateful = system.actorOf(Props(new StatefulService()))

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "An api query helper" must {
    "respond to queries and commands" in {
      val start = 0
      val incr = 5
      val decr = 2

      val future = for {
        _ <- stateful command SetValue(start)
        _ <- stateful command IncrementValue(incr)
        _ <- stateful command DecrementValue(decr)
        checkIt <- stateful query QueryValue()
      } yield {
        checkIt
      }

      Await.result(future, 1 second) shouldBe incr - decr
    }

    "report errors" in {
      val future = stateful command BadCommand() map { x =>
        // should not execute
        ???
      } recover {
        case CouldNotExecute =>
        // handle
      }

      Await.result(future, 1 second)
    }

  }
}
