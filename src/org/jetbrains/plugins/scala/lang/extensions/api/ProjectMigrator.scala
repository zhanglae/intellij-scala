package org.jetbrains.plugins.scala.lang.extensions.api

import org.jetbrains.plugins.scala.lang.extensions.api.base.{IdeaProject, SourceFile}

/**
  * User: Dmitry.Naydanov
  * Date: 16.02.17.
  */
abstract class ProjectMigrator extends TreeConverterBase {
  def convertLocal(file: SourceFile)
  def convertGlobal(project: IdeaProject)
}
