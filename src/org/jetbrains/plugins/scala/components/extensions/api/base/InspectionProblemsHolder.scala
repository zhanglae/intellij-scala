package org.jetbrains.plugins.scala.components.extensions.api.base

import scala.meta.Tree

/**
  * User: Dmitry.Naydanov
  * Date: 16.02.17.
  */
trait InspectionProblemsHolder {
  def registerProblem(where: Tree, message: String, fixes: Seq[Tree => Tree]): Unit
}
