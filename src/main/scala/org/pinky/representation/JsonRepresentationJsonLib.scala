package org.pinky.representation

import java.io.{BufferedWriter, OutputStreamWriter, OutputStream}
import org.json.JSONObject

class JsonRepresentationJsonLib extends Representation {
  def write(data: Map[String, AnyRef], out: OutputStream) = {
    val outWriter = new BufferedWriter(new OutputStreamWriter(out))
    val jMap = new java.util.HashMap[String, AnyRef]
    data foreach  { case (k, v) => jMap.put(k, v) }

    outWriter.write(new JSONObject(jMap).toString)
    outWriter.close
  }

}
