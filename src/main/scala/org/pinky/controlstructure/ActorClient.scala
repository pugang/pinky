package org.pinky.controlstructure

import java.io.PrintWriter
import java.lang.Runnable
import java.util.concurrent.{TimeUnit, ThreadPoolExecutor, ScheduledThreadPoolExecutor}
import java.util.regex.Pattern
import javax.servlet.ServletResponse
import javax.servlet.http.{HttpServletResponse, HttpServlet, HttpServletRequest}
import org.eclipse.jetty.continuation.Continuation
import org.pinky.annotation.Every
import org.pinky.annotation.Every
import se.scalablesolutions.akka.actor.Actor

case object UnSchedule
case object Stop

trait Client extends Actor {
  var workers: List[Actor] = Nil

  val executor = new ScheduledThreadPoolExecutor(10, new ThreadPoolExecutor.AbortPolicy())
  val delay: Every = this.getClass().getAnnotation(classOf[Every])

  def fireStarter(block: => Unit): Unit = {
    if (!this.isRunning) startLink(this)
    if (delay != null) {
      executor.scheduleWithFixedDelay(
        new Runnable { def run = block },
        parseDuration(delay.value()),
        parseDuration(delay.value()),
        TimeUnit.SECONDS)
    } else {
      block
    }
  }

  override def shutdown = {
    workers.filter(_.isRunning).foreach(unlink)
    if (this.isRunning) unlink(this)
    executor.shutdown
  }

  override def receive = {
    case UnSchedule => shutdown
    case Stop => stop
    case _ => println("could not understand this message")
  }

  /*
   * scheduler is modelled after <a href="http://playframework.org">Play!</a>
   */
  def parseDuration(duration: String): Int = {
    import SchedulerPatterns._

    if (duration == null) {
      60 * 60 * 24 * 365
    }
    var toAdd = -1
    if (days.matcher(duration).matches()) {
      val matcher = days.matcher(duration)
      matcher.matches()
      toAdd = Integer.parseInt(matcher.group(1)) * (60 * 60) * 24
    } else if (hours.matcher(duration).matches()) {
      val matcher = hours.matcher(duration)
      matcher.matches()
      toAdd = Integer.parseInt(matcher.group(1)) * (60 * 60)
    } else if (minutes.matcher(duration).matches()) {
      val matcher = minutes.matcher(duration)
      matcher.matches()
      toAdd = Integer.parseInt(matcher.group(1)) * (60)
    } else if (seconds.matcher(duration).matches()) {
      val matcher = seconds.matcher(duration)
      matcher.matches()
      toAdd = Integer.parseInt(matcher.group(1))
    }
    if (toAdd == -1) {
      throw new IllegalArgumentException("Invalid duration pattern : " + duration)
    }
    toAdd
  }
  private object SchedulerPatterns {
    val days = Pattern.compile("^([0-9]+)d$")
    val hours = Pattern.compile("^([0-9]+)h$")
    val minutes = Pattern.compile("^([0-9]+)mn$")
    val seconds = Pattern.compile("^([0-9]+)s$")
  }
}


trait ActorClient extends Client {
  def callback(reqData: Map[String, AnyRef])

  def fireStarter(reqData: Map[String, AnyRef]): Map[String, AnyRef] = {
    workers.filter(!_.isRunning).foreach(startLink)
    super.fireStarter(callback(reqData))
    reqData
  }
}


case object Resume

abstract class ActorCometClient(continuation:Continuation, request: HttpServletRequest) extends Client {
  def callback
 
  workers.filter(!_.isRunning).foreach(startLink) 

  
  def handler(messageHandler: PartialFunction[Any, Unit]): PartialFunction[Any, Unit] = {
    val resumeHandler: PartialFunction[Any, Unit] = {
      case Resume => {
        val out = continuation.getServletResponse.getWriter
        out.flush()
        out.close()
        shutdown
        continuation.complete()
      }
    }
    resumeHandler orElse messageHandler orElse super.receive 
  }

  def writer(continuation:Continuation):PrintWriter =
    continuation.getServletResponse.getWriter
}


