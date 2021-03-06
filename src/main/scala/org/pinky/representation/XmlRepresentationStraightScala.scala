package org.pinky.representation

import java.io.{BufferedWriter, OutputStreamWriter, OutputStream}
import scala.collection.JavaConversions._

class XmlRepresentationStraightScala extends Representation {
  def write(dataWithTemplate: Map[String, AnyRef], out: OutputStream) = {
    val data = dataWithTemplate - "template"
    val outWriter = new BufferedWriter(new OutputStreamWriter(out))
    val xml = new StringBuffer("<map>")
    data.foreach((entry) =>
      {
        xml.append("<entry>\n<java.lang.String>" + entry._1 + "</java.lang.String> \n"
                + "<" + entry._2.getClass.getName + ">" + entry._2 + "</" + entry._2.getClass.getName + ">\n</entry>")
      }
      )
    xml.append("</map>")
    outWriter.write(xml.toString)
    outWriter.close

  }


}
