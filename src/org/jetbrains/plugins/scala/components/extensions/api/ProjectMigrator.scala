package org.jetbrains.plugins.scala.components.extensions.api

import org.jetbrains.plugins.scala.components.extensions.api.base.{IdeaProject, SourceFile}

/**
  * User: Dmitry.Naydanov
  * Date: 16.02.17.
  */
abstract class ProjectMigrator extends NamedExtensionComponent {
  def convertLocal(file: SourceFile)
  def convertGlobal(project: IdeaProject)
}
