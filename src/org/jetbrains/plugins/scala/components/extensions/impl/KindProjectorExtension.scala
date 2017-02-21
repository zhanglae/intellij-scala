package org.jetbrains.plugins.scala.components.extensions.impl

import org.jetbrains.plugins.scala.components.extensions.ScalaPluginExtension
import org.jetbrains.plugins.scala.components.extensions.api._

import scala.meta._

/**
  * @author mutcianm
  * @since 21.02.17.
  */
class KindProjectorExtension(api: BaseApiProvider) extends ScalaPluginExtension(api) {
  override def extensionName = "KindProjectorExtension"
  override def extensionDescription = "Support for kind projector syntax in IntelliJ IDEA"

  class TypeElementRewriter extends ArbitraryTreeTransformer {
    override def name = "KindProjector TypeElementRewriter"
    override def description = "KindProjector TypeElementRewriter description"

    override def convert(tree: Tree): Tree = {
      tree match {
        case q"Blah[..$_]" => q"Blah[Int]"
        case _ => tree
      }
      tree
    }

    override def context = ApplicabilityContext(classOf[Term.ApplyType], isApplicable)

    private def isApplicable(tree: Tree): Boolean = {
      tree match {
        case q"Blah[..$_]" => true
        case _ => false
      }
    }
  }
}
