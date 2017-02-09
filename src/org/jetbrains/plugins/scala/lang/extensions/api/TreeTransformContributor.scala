package org.jetbrains.plugins.scala.lang.extensions.api

import scala.meta._

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class TreeTransformContributor extends ExtensionContributorBase {
  def transform(tree: Tree): Tree
}
