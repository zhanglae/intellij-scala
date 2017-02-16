package org.jetbrains.plugins.scala.lang.extensions.api

import scala.meta.Tree

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class ArbitraryTreeConverter extends TreeConverterBase {
  def convert(tree: Tree): Tree
}
