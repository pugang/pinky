package org.pinky.form.builder

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import net.sf.oval.ConstraintViolation
import net.sf.oval.constraint.Length
import org.pinky.annotation.form._
import org.pinky.validator.{CustomOvalValidator}
import scala.collection.JavaConversions._

/**
 * defines default behaviour for prepopulating and rendering forms
 */
private[form] trait Default {

  private val methods = getClass.getMethods
  private val fields = getClass.getDeclaredFields
  private val setters =
    Map(methods.filter(_.getName.endsWith("_$eq")) map { method =>
          method.getName.replace("_$eq", "") -> method
        }:_*)
  private val getters = 
    Map(methods.filter(! _.getName.endsWith("_$eq")) map { method =>
          method.getName -> method
        }:_*)

  /**
   * @request Params incoming request param's param map setting form data using
   *                 reflection 
   */
  def loadRequestOld(requestParams: Map[String, Seq[String]]) = {
    for ((key, paramValues) <- requestParams) {
      for (setter <- this.getClass.getMethods if (setter.getName.toLowerCase.contains(key.toLowerCase + "_$eq"))) {
        if (isComplexWidget(setter)) {
          // first get the current field map if any
          for (getter <- this.getClass.getMethods if (getter.getName.toLowerCase == setter.getName.toLowerCase.replace("_$eq", ""))) {
            var currentField = getter.invoke(this).asInstanceOf[Map[String, Boolean]]
            //set values whenever is possible
            if (currentField != null) {
              for (param <- paramValues) {
                for (item <- currentField) {
                  if (item._1 == param) currentField(item._1) = true
                }
              }
              //save field
              setter.invoke(this, currentField)
            }
          }
        } else {
          setter.invoke(this, paramValues(0))
        }
      }
    }
  }

  def loadRequest(requestParams: Map[String, Seq[String]]) = {
    requestParams foreach { case(key, paramValues) =>
      setters.get(key) match {
        case Some(setter) =>
          if(isComplexWidget(setter)) {
            // First, get the current field map if any
          } else {
            setter.invoke(this, paramValues(0))
          }
        case None =>
      }
    }
  }


  /**
   *  by complex widget we mean widgets that can vary in terms of size
   */
  private def isComplexWidget(method: Method): Boolean = {
    for (annotation <- method.getDeclaredAnnotations
         if ((annotation.annotationType == classOf[CheckBox] ||
                 annotation.annotationType == classOf[DropDown] ||
                 annotation.annotationType == classOf[RadioButton]
                 )
                 )
    ) {
      return true
    }
    return false
  }


  /**
   *  defines standard control structure for building a form
   * @form the form class
   */
  private[form] def basedOn(form: Form, starttag: String, endtag: String): String = {
    var formBody = new StringBuffer()
    var action = ""
    var formType = "application/x-www-form-urlencoded"
    // add fields

    for (setter <- form.getClass.getMethods if setter.getName.contains("_$eq")) {
      for (getter <- form.getClass.getMethods if getter.getName == setter.getName.replace("_$eq", "")) {
        //check to see whether a custom action attribute needs to be set
        if (getter.getName.startsWith("action")) action = getter.invoke(form).toString
        for (annotation <- getter.getDeclaredAnnotations) {
          //check to see whether the form should be multipart
          if (annotation.annotationType == classOf[Upload]) formType = "multipart/form-data"
          //build the widget field
          widget(form, annotation, getter) match {
            case Some(field) => formBody.append(starttag + "\n" + generateLabelFor(getter) + "\n" + scala.xml.Unparsed(field) + "\n" + endtag + "\n")
            case None =>
          }
        }
      }
    }
    //assemble the final form
    <form action={action} method="POST" enctype={formType}>
      {scala.xml.Unparsed(formBody.toString)}
    </form>.toString
  }

  /**
   * @form incoming form
   * @annotation the annotation on the widget
   * @method which is referencing the current widget
   * renders the widget
   */
  private[form] def widget(form: Form, annotation: Annotation, method: Method): Option[String] = annotation match {

    case a: Length =>
      Some(<input id={"id_" + method.getName.toLowerCase} type="text" size={if (a.max <= 20) a.max.toString else "20"} maxlength={a.max.toString} name={method.getName.toLowerCase} value={method.invoke(form).toString}/>.toString)

    case a: Hidden =>
      Some(<input id={"id_" + method.getName.toLowerCase} type="hidden" name={method.getName.toLowerCase} value={method.invoke(form).toString}/>.toString)

    case a: Upload =>
      Some(<input id={"id_" + method.getName.toLowerCase} type="file" size="40" name={method.getName.toLowerCase} value={method.invoke(form).toString}/>.toString)

    case a: TextArea =>
      Some(<textarea id={"id_" + method.getName.toLowerCase} name={method.getName.toLowerCase} rows={a.rows.toString} cols={a.cols.toString}/>.toString)

    case a: DropDown => {
      //return nothing if the return type does not match
      if (method.getReturnType == classOf[Map[String, Boolean]]) {
        val optionTags = new StringBuffer()
        val map = method.invoke(form).asInstanceOf[Map[String, Boolean]]
        if (map == null || map.size == 0) throw new Exception("DropDown field needs at least one item")
        for ((key, value) <- map) {
          if (value)
            optionTags.append(<option value={key.toLowerCase} selected=" ">
              {key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase}
            </option> + "\n")
          else
            optionTags.append(<option value={key.toLowerCase}>
              {key.substring(0, 1).toUpperCase() + key.substring(1)}
            </option> + "\n")
        }
        if (a.multi)
          Some(<select name={method.getName.toLowerCase} multiple=" ">
            {scala.xml.Unparsed(optionTags.toString)}
          </select>.toString)
        else
          Some(<select name={method.getName.toLowerCase}>
            {scala.xml.Unparsed(optionTags.toString)}
          </select>.toString)
      } else
        throw new Exception("a DropDown should have a type of Map[String, Boolean]")
    }

    case a: RadioButton => {
      if (method.getReturnType == classOf[Map[String, Boolean]]) {
        val optionTags = new StringBuffer()
        val map = method.invoke(form).asInstanceOf[Map[String, Boolean]]
        if (map == null || map.size == 0) throw new Exception("Radiobutton field needs at least one item")
        for ((key, value) <- map) {
          if (value)
            optionTags.append(<input type="radio" name={method.getName.toLowerCase} value={key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase} selected=" "/> + "\n")
          else
            optionTags.append(<input type="radio" name={method.getName.toLowerCase} value={key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase}/> + "\n")
        }
        Some(optionTags.toString)
      } else
        throw new Exception("a RadioButton should have a type of Map[String, Boolean]")
    }

    case a: CheckBox => {
      if (method.getReturnType == classOf[Map[String, Boolean]]) {
        val optionTags = new StringBuffer()
        val map = method.invoke(form).asInstanceOf[Map[String, Boolean]]
        if (map == null || map.size == 0) throw new Exception("CheckBox field needs at least one item")
        for ((key, value) <- map) {
          if (value)
            optionTags.append(<input type="checkbox" name={method.getName.toLowerCase} value={key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase} selected=" "/> + "\n")
          else
            optionTags.append(<input type="checkbox" name={method.getName.toLowerCase} value={key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase}/> + "\n")
        }
        Some(optionTags.toString)
      } else
        throw new Exception("a CheckBox should have a type of Map[String, Boolean]")
    }

    case _ => None
  }

  private[form] def generateLabelFor(getter: Method): String = <label for={"id_" + getter.getName.toLowerCase}>
    {getter.getName.toLowerCase + ":"}
  </label>.toString

}

/**
 * establishes a main base class for all form actions
 */
abstract class Form

/**
 * describes the main behavior of a builder
 */
trait Builder {
  /**
   * renders the form
   */
  def render: String

  /**
   * prepopulates bean data based on a request's requestParamMap
   *
   * @requestParam incoming request params
   */
  def loadRequest(requestParams: Map[String, Seq[String]])
}


/**
 * provides a form builder which outputs a form wrapped in a <cc><tr><td></cc>,
 * note, you will need to provide the corresponding <table> tag
 */
trait TableBuilder extends Form with Builder with Default {
  override def render: String = basedOn(this, "<tr><td>", "</td></tr>")
}

/**
 * provides a form builder which outputs a form wrapped in a paragraph tag
 */

trait ParagraphBuilder extends Form with Builder with Default {
  override def render: String = basedOn(this, "<p>", "</p>")
}

/**
 * provides a form builder which outputs a form wrapped in a
 * <pre><li></li></pre> tag, note, you will need to provide the corresponding
 * <ul> tag
 */

trait UlTagBuilder extends Form with Builder with Default {
  override def render: String = basedOn(this, "<li>", "</li>")
}


/**
 * provides validation using oval framework
 */
trait Validator {

  /**
   * @return returns a java list[Map] because it's most likely used from a
   * java templating enginge
   */
  def validate: Seq[Map[String, String]] = 
    Factory.validator.validateFor(this).map(extract)

  private def extract(m: ConstraintViolation): Map[String, String] = {
    val message = m.getMessage
    val idxEnd = message.lastIndexOf(".") + 1
    val k = message.substring(idxEnd, message.indexOf(" "))
    val v = message.substring(idxEnd)
    Map(k -> v)
  }

  private object Factory {
    val validator = new CustomOvalValidator()
  }
}
