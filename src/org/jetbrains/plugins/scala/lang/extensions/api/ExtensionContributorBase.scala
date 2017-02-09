package org.jetbrains.plugins.scala.lang.extensions.api

import scala.meta.Tree

/**
  * @author mutcianm
  * @since 09.02.17.
  */
trait ExtensionContributorBase {
  def isApplicable(elementType: Class[_ <: Tree], predicate: (Tree => Boolean) = _ => true): Boolean
}
