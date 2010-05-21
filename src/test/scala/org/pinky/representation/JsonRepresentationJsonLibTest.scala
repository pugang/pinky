package org.pinky.representation

import java.io.ByteArrayOutputStream
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers


class JsonRepresentationJsonLibTest extends Spec with ShouldMatchers {
  describe ("A json lib  based json representation") {
        it("should_render_the_data") {
      val out = new ByteArrayOutputStream()
      val data = Map("message" -> "hello world")
      val representation = new JsonRepresentationJsonLib()
      representation.write(data, out)
      out.toString should equal("{\"message\":\"hello world\"}")
    }

   }

}
