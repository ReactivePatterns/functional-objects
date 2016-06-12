# Your Object As a Function

The logic of a stateful component can be represented as a Scala PartialFunction that processes commands/events to 
update the component state:

    
      val processor: PartialFunction[(Int, StateCommand), Int] = {
        case (current: Int, command: SetValue) => command.newValue
        case (current: Int, command: IncrementValue) => current + command.value
        case (current: Int, command: DecrementValue) => current - command.value
      }
      
The component maintains state, processes events and publishes a value to subscribers or dependencies.

Wrapping the logic into a component can be done with a (persistent) Akka actor that handles the boilerplate, so the
usage is simple and focuses on the logic:

      class StatefulService extends SimpleComponent[Int, StateCommand](0, processor)
     
      val stateful = system.actorOf(Props(new StatefulService()))
     
The events (commands and queries) are defined as follows:
 
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

Usage:

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
            
# Credit
[https://github.com/digital-magic-io/akka-cqrs-dsl](https://github.com/digital-magic-io/akka-cqrs-dsl)

[https://github.com/dnvriend/akka-persistence-inmemory](https://github.com/dnvriend/akka-persistence-inmemory)