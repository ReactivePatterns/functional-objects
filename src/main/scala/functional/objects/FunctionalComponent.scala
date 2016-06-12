package functional.objects

import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.persistence.{AtLeastOnceDelivery, PersistentActor}
import functional.objects.API._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

/**
  * Created by stanlea on 6/12/16.
  */

class FunctionalComponent[State, Event: ClassTag, Value]
(initial: State, processor: PartialFunction[(State, Event), State], transformer: State => Value)
(implicit ec: ExecutionContext) extends PersistentActor with ActorLogging with AtLeastOnceDelivery {

  val eventClass = implicitly[ClassTag[Event]].runtimeClass

  protected var state: State = initial

  protected def handleEvent(event: Event): Unit = {
    state = processor(state, event)
  }

  override def receiveCommand: Receive = LoggingReceive {
    case event: Event if eventClass.isInstance(event) => {
      persist(event)(handleEvent)
      //handleEvent(event)
      //saveSnapshot(state)
    }
  }

  override def receiveRecover: Receive = LoggingReceive {
    case event: Event if eventClass.isInstance(event) => {
      handleEvent(event)
    }
  }

  override def persistenceId: String = this.getClass.getSimpleName
}

class CQRSComponent[State, Event <: Request[Value] : ClassTag, Value: ClassTag]
(initial: State, processor: PartialFunction[(State, Event), State], transformer: State => Value)
  extends FunctionalComponent(initial, processor, transformer) {

  override def receiveCommand: Receive = LoggingReceive {
    case query: Query[Value] => sender() ! query.found(transformer(state))
    case event: Command[Value] if eventClass.isInstance(event) => {
      super.receiveCommand.apply(event)
      sender() ! CommandResult(Right(transformer(state))) //publish value
    }
    case command: Command[Value] => sender() ! command.failure(CouldNotExecute)
  }
}

//class CQRSComponent[State, Event <:  Request[Value] :ClassTag, Value :ClassTag]
//(initial: State, processor: PartialFunction[(State, Event), State], transformer: State => Value)
//  extends Actor {
//
//  val eventClass = implicitly[ClassTag[Event]].runtimeClass
//
//  override def receive(): Receive = receive(initial)
//
//  def receive(state: State): Receive = {
//    case query: Query[Value] => sender() ! query.found(transformer(state))
//    case event: Command[Value] if eventClass.isInstance(event) => {
//      val newState: State = processor(state, event.asInstanceOf[Event])
//      sender() ! CommandResult(Right(transformer(newState))) //publish value
//      context.become(receive(newState))
//    }
//    case command: Command[Value] =>  sender() ! command.failure(CouldNotExecute)
//  }
//}


class SimpleComponent[State: ClassTag, Event <: Request[State] : ClassTag](initial: State, processor: PartialFunction[(State, Event), State])
  extends CQRSComponent[State, Event, State](initial, processor, identity[State])


