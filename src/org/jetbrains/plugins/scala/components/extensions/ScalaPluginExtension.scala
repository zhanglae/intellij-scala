package org.jetbrains.plugins.scala.components.extensions

import org.jetbrains.plugins.scala.components.extensions.api.base._
import org.jetbrains.plugins.scala.components.extensions.api._

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
  def treeTransformers: Seq[ArbitraryTreeTransformer] = Seq.empty
  def typeTransformers: Seq[ArbitraryTypeTransformer] = Seq.empty
  // TODO: other extensions
}
