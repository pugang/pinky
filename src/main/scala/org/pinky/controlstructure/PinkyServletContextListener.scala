package org.pinky.controlstructure

import com.google.inject.Injector
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.Module
import com.google.inject.Guice

/**
 * adds varargs support to guice injector creator, also hides guice form the
 * listener
 *
 * @author peter hausel gmail com (Peter Hausel)
 */
abstract class PinkyServletContextListener extends GuiceServletContextListener {
  var modules: Array[Module] = _

  /**
   * creates a guice injector from modules passed in via modules Array, without
   * the array this thing is not functioning
   *
   * @return Injector
   */
  override protected def getInjector(): Injector = {
    Guice.createInjector(modules: _*)
  }
}
