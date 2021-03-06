package org.pinky.representation

import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.util.Date
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers


class RssRepresentationTest extends Spec with ShouldMatchers {
  describe("An RSS representation") {
    it("should_fail_due_to_missing_items") {
      var exceptionIsThrown = false
      try {
        val out = new ByteArrayOutputStream();
        val data = Map("message" -> "hello world")
        val representation = new RssRepresentation()
        representation.write(data, out)
      } catch {
        case ex: java.util.NoSuchElementException => exceptionIsThrown = true
        case _ =>
      }
      exceptionIsThrown should be(true)
    }

    it("should_fail_since_item_type_is_not_list_rss_items") {
      var exceptionIsThrown = false
      try {
        val out = new ByteArrayOutputStream();
        val data = Map("rssitems" -> "hello world")
        val representation = new RssRepresentation()
        representation.write(data, out)
      } catch {
        case ex: java.lang.ClassCastException => exceptionIsThrown = true
        case _ =>
      }
      exceptionIsThrown should be(true)
    }

    it("should_render_rss") {
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

      //setup return values
      val data = Map(
            "rssitems" -> rssList,
            "rssheader" -> rssHeader)
      val out = new ByteArrayOutputStream();

      val representation = new RssRepresentation()
      representation.write(data, out)
      out.toString.contains("<title>Test</title>") should be(true)
      out.toString.contains("<description>MISC MISC</description>") should be(true)
      out.toString.contains("<guid>http://localstation.com/item11#1</guid>") should be(true)
    }
  }
}


