package org.jetbrains.plugins.scala.components.extensions.api
import scala.meta._

/**
  * @author mutcianm
  * @since 02.03.17.
  */
abstract class ArbitraryTypeTransformer extends ArbitraryTreeTransformer {
  override def transform(tree: Tree) = None
  def suggestType(tree: Tree): Option[Type]
}
