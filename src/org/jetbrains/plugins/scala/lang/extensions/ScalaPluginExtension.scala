package org.jetbrains.plugins.scala.lang.extensions

import org.jetbrains.plugins.scala.lang.extensions.api.base.MigrationDescriptor
import org.jetbrains.plugins.scala.lang.extensions.api.{BaseApiProvider, InspectionConverter, ProjectMigrator, ArbitraryTreeConverter}

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class ScalaPluginExtension {
  def version: Int
  def extensionName: String
  def extensionDescription: String
  // TODO: compatible libraries?

  /* Platform API entrypoint for developers */
  def api: BaseApiProvider

  /* Exported extensions */
  def inspections:  Seq[InspectionConverter]
  def migrators(migrationDescriptor: MigrationDescriptor): Seq[ProjectMigrator]
  def transformers: Seq[ArbitraryTreeConverter]
  // TODO: other extensions
}
