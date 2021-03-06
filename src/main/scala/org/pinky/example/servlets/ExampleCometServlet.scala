package org.pinky.example.servlets

import org.pinky.comet.CometServlet
import javax.servlet.http.HttpServletRequest
import com.google.inject._
import java.net.URL
import scala.io.Source.fromInputStream
import org.pinky.controlstructure.{Resume, ActorCometClient}
import org.eclipse.jetty.continuation.Continuation
import scala.xml._
import org.apache.commons.io.IOUtils
import org.pinky.util.ARM.using

/**
 * Created by IntelliJ IDEA.
 * User: phausel
 * Date: Jan 24, 2010
 * Time: 10:15:31 PM
 * To change this template use File | Settings | File Templates.
 */

class MyActorCometClient(continuation: Continuation, request: HttpServletRequest)
        extends ActorCometClient(continuation, request) {
  val url = new URL("http://twitter.com/statuses/user_timeline/5047741.rss")
  override def receive = handler {
    case "readfeed" => {
      for (in <- using(url.openStream)) {
        val stuff = for (line <- (XML.loadString(IOUtils.toString(in)) \\ "title")) yield line.text
        writer(continuation).println(stuff.mkString("<br>"))
      }
    }
  }

  def callback = {
    println("about to hit callback")
    Thread.sleep(1000)
    this ! "readfeed"
    println("about to hit second sleep")
    Thread.sleep(1000)
    this ! Resume
  }
}
@Singleton class ExampleCometServlet extends CometServlet[MyActorCometClient]