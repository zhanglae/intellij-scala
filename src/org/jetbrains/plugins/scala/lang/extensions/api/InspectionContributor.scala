package org.jetbrains.plugins.scala.lang.extensions.api

import scala.meta.Tree

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class InspectionContributor extends ExtensionContributorBase {
  def suggestQuickFix(tree: Tree): Tree
}
