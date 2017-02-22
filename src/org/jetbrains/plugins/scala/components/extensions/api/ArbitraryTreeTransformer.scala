package org.jetbrains.plugins.scala.components.extensions.api

import scala.meta.Tree

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class ArbitraryTreeTransformer extends NamedExtensionComponent {
  def transform(tree: Tree): Option[Tree]
  def context: ApplicabilityContext
  def additionalDeclarations(hint: Option[Tree] = None) = None
}
