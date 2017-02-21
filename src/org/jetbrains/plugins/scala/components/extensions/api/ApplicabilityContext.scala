package org.jetbrains.plugins.scala.components.extensions.api

import scala.meta.Tree

/**
  * @author mutcianm
  * @since 21.02.17.
  */
case class ApplicabilityContext(elementType: Class[_ <: Tree], predicate: (Tree => Boolean) = _ => false)
