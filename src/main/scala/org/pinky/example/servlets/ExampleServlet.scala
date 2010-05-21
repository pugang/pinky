package org.pinky.example.servlets

import com.google.inject._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.pinky.controlstructure.{ActorClient, Dispatch}
import se.scalablesolutions.akka.actor.{ Actor}

class PingActor(pong: Actor) extends Actor {
  def receive = {
    case "Jonas" => {
      pong ! "whatsnext"
    }
    case "yay" => {
      println("replay from Pong")
    }
    case _ => println("Ping:unknown message was received")
  }
}
class PongActor extends Actor {
  def receive = {
    case "whatsnext" => {
      println("pong")
      reply("yay")
    }
    case _ => println("Pong:unknown message was received")
  }
}

trait Workers {
  this: ActorClient=>
  private val pongActor = new PongActor
  workers = List(new PingActor(pongActor), pongActor)
}

class PingPongClient extends ActorClient with Workers {
  def callback(reqData: Map[String, AnyRef]) {
    workers(0) ! reqData("name")
  }
}


/**
 * A regular controller(serlvet) example
 *
 * @author peter hausel gmail com (Peter Hausel)
 */

@Singleton
class ExampleServlet @Inject()(dispatch: Dispatch, actorClient: ActorClient) extends HttpServlet   {
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) = {
    dispatch.call(request, response) {
      val name = request.getParameter("name") match {
        case null => "default"
        case other => other
      }

      Map("name" -> name)
    }
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) = {
    dispatch.call(request, response) {
      Map("name" -> "Changing state with POST")
    }

  }

}
