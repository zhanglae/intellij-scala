package org.jetbrains.plugins.scala.components.extensions.api

import org.jetbrains.plugins.scala.components.extensions.api.base.InspectionProblemsHolder

import scala.meta.Tree

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class InspectionConverter extends NamedExtensionComponent {
  def actionFor(holder: InspectionProblemsHolder): PartialFunction[Tree, Any]
}
