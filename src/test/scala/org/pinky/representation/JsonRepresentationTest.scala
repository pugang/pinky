package org.pinky.representation

import java.io.ByteArrayOutputStream
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers  

class JsonRepresentationTest extends Spec with ShouldMatchers {
  describe("A JSON representation") {
    it("should_render_the_data") {
      val out = new ByteArrayOutputStream();
      val data = Map("message" -> "hello world")
      val representation = new JsonRepresentation()
      representation.write(data, out)
      out.toString should equal("{\"map\": [\n  [\n    \"message\",\n    \"hello world\"\n  ]\n]}")
    }
  }
}
