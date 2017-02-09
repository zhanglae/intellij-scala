package org.jetbrains.plugins.scala.lang.extensions.api

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class SimpleInspection extends TreeTransformContributor {
  def message: String
}
