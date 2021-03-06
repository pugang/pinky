package org.pinky.controlstructure

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.mockito.Mockito._
import org.pinky.representation.{Representation, Representations}
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers


class DispatchTest extends Spec with ShouldMatchers {
  var out: javax.servlet.ServletOutputStream = _
  var representation: Representations = _
  var request: HttpServletRequest = _
  var response: HttpServletResponse = _
  var modes: Map[String, Representation] = _
  var contentypes: Map[String, String] = _

  def mock_context() {
    out = mock(classOf[javax.servlet.ServletOutputStream])
    representation = mock(classOf[Representations])
    request = mock(classOf[HttpServletRequest])
    response = mock(classOf[HttpServletResponse])
    modes = mock(classOf[Map[String, Representation]])
    contentypes = mock(classOf[Map[String, String]])
    return
  }

  describe("A Default Control") {
    it("should_fail_due_to_bad_extentions") {
      mock_context()
      //setup expectations
      when(request.getContextPath).thenReturn("")
      when(representation.mode).thenReturn(modes)
      when(representation.contentType).thenReturn(contentypes)
      when(modes("kvv")).thenThrow(new java.util.NoSuchElementException)
      when(contentypes("kvv")).thenReturn("text/funky")
      when(request.getRequestURI).thenReturn("/hello/index.kvv")
      when(response.getOutputStream).thenReturn(out)
      //now run the actual test
      val control = new DefaultControl(representation)
      control.call(request, response) {
        Map("message" -> "hello world")
      }
      verify(response).setStatus(500)

    }

    it("should_render_html_even_withot_explicit_extension") {
      //setup expectation
      mock_context()
      val concreteRepresentation = mock(classOf[Representation])
      when(request.getContextPath).thenReturn("")
      when(representation.mode).thenReturn(modes)
      when(representation.contentType).thenReturn(contentypes)
      when(modes("html")).thenReturn(concreteRepresentation)
      when(contentypes("html")).thenReturn("text/html")
      when(request.getRequestURI).thenReturn("/hello/index")
      when(response.getOutputStream).thenReturn(out)
      //now run the actual test
      val control = new DefaultControl(representation)
      control.call(request, response) {
        Map("message" -> "hello world")
      }
      val assumed = Map(
        "message" -> "hello world",
        "template" -> "/hello/index")
      verify(response).setContentType("text/html")
      verify(response, never).setStatus(500)
      verify(concreteRepresentation).write(assumed, out)
    }


    it("should_render_html_even_wit_explicit_extension") {
      //setup expectation
      mock_context()
      val concreteRepresentation = mock(classOf[Representation])
      when(request.getContextPath).thenReturn("")
      when(representation.mode).thenReturn(modes)
      when(representation.contentType).thenReturn(contentypes)
      when(modes("rss")).thenReturn(concreteRepresentation)
      when(contentypes("rss")).thenReturn("text/rss")
      when(request.getRequestURI).thenReturn("/hello/index.rss")
      when(response.getOutputStream).thenReturn(out)
      //now run the actual test
      val control = new DefaultControl(representation)
      control.call(request, response) {
        Map("message" -> "hello world")
      }
      val assumed = Map("message" -> "hello world")
      verify(response).setContentType("text/rss")
      verify(response, never).setStatus(500)
      verify(concreteRepresentation).write(assumed, out)
    }
  }
}
