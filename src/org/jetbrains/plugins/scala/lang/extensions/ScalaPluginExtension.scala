package org.jetbrains.plugins.scala.lang.extensions

import org.jetbrains.plugins.scala.lang.extensions.api.{BaseApiProvider, InspectionContributor, TreeTransformContributor}

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
  def Transformers: Seq[TreeTransformContributor]
  def Inspections:  Seq[InspectionContributor]
  // TODO: other extensions
}
