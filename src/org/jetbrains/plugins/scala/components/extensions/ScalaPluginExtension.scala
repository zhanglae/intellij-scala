package org.jetbrains.plugins.scala.components.extensions

import org.jetbrains.plugins.scala.components.extensions.api.base.MigrationDescriptor
import org.jetbrains.plugins.scala.components.extensions.api.{BaseApiProvider, InspectionConverter, ProjectMigrator, ArbitraryTreeTransformer}

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class ScalaPluginExtension (protected val api: BaseApiProvider) {
//  def version: Int
  def extensionName: String
  def extensionDescription: String
  // TODO: compatible libraries?

  /* Exported extensions */
  def inspections:  Seq[InspectionConverter] = Seq.empty
  def migrators(migrationDescriptor: MigrationDescriptor): Seq[ProjectMigrator] = Seq.empty
  def transformers: Seq[ArbitraryTreeTransformer] = Seq.empty
  // TODO: other extensions
}
