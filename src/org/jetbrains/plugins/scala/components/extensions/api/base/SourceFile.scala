package org.jetbrains.plugins.scala.components.extensions.api.base

import scala.meta._

/**
  * User: Dmitry.Naydanov
  * Date: 16.02.17.
  */
trait SourceFile {
  def name: String
  def root: Tree
}
