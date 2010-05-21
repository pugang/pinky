package org.pinky.example.servlets

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import com.google.inject._
import java.text.DateFormat
import java.util.Date

import org.pinky.controlstructure.Dispatch
import org.pinky.representation.{RssItem, RssHeader}

/**
 * An rss controller(serlvet) exmaple
 *
 * @author peter hausel gmail com (Peter Hausel)
 */

@Singleton
class ExampleRssServlet @Inject()(dispatch: Dispatch) extends HttpServlet {
  override def doGet(req: HttpServletRequest,
                     res: HttpServletResponse) =
    {
      dispatch.call(req, res) {
        //today's date
        val now = new Date();
        val today = DateFormat.getDateTimeInstance(
          DateFormat.LONG, DateFormat.LONG).format(now)

        //create  header
        var rssHeader = new RssHeader("Test", "http://lolstation.com", "MISC MISC", today, today, "(C) 2009", "en-us")

        //create items
        val rssList = List[RssItem](
          new RssItem("item title", "http://localstation.com/item11", "description",
            today, "http://localstation.com/item11#1")
          )

        // setup return values
        Map("rssitems" -> rssList,
            "rssheader" -> rssHeader)
      }

    }

}
