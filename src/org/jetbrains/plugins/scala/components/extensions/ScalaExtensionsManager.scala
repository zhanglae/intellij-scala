package org.jetbrains.plugins.scala.components.extensions

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.components.extensions.api.ArbitraryTreeTransformer
import org.jetbrains.plugins.scala.components.extensions.api.impl.IdeaApiProvider
import org.jetbrains.plugins.scala.components.extensions.impl.KindProjectorExtension
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScParameterizedTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScMethodCall}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScObject, ScTrait}

import scala.meta._

class ScalaExtensionsManager(project: Project) extends ProjectComponent {
  import ScalaExtensionsManager._

  override def projectOpened(): Unit = loadExtensions()
  override def projectClosed(): Unit = ()
  override def initComponent(): Unit = ()
  override def disposeComponent(): Unit = ()
  override def getComponentName: String = "ScalaExtensionsManager"

  def getExtensions: Seq[ScalaPluginExtension] = Seq(new KindProjectorExtension(new IdeaApiProvider(project)))
  def getApplicableExtensions(elem: ScalaPsiElement): Seq[ScalaPluginExtension] = getExtensions
  def getTransformersForElement(elem: ScalaPsiElement): Seq[ArbitraryTreeTransformer] = {
    transformersByContext.getOrElse(psiToMeta.getOrElse(elem.getClass, classOf[Tree]), Seq.empty)
  }

  private def loadExtensions(): Unit = {
    for {
      extension <- getExtensions
      tr <- extension.transformers
    } {
      transformersByContext(tr.context.elementType) = transformersByContext.getOrElse(tr.context.elementType, Seq.empty)
    }
  }

  private val transformersByContext: scala.collection.mutable.Map[Class[_ <: Tree], Seq[ArbitraryTreeTransformer]] = Map()

}

object ScalaExtensionsManager {
  def getInstance(project: Project): ScalaExtensionsManager = project.getComponent(classOf[ScalaExtensionsManager])

  val metaToPsi: Map[Class[_ <: Tree], Class[_ <: ScalaPsiElement]] = Map(
    classOf[Tree]           -> classOf[ScalaPsiElement],
    classOf[Term]           -> classOf[ScExpression],
    classOf[Defn.Class]     -> classOf[ScClass],
    classOf[Defn.Trait]     -> classOf[ScTrait],
    classOf[Defn.Object]    -> classOf[ScObject],
    classOf[Term.Apply]     -> classOf[ScMethodCall],
    classOf[Term.ApplyType] -> classOf[ScParameterizedTypeElement]
  )

  val psiToMeta: Map[Class[_ <: ScalaPsiElement], Class[_ <: Tree]] = metaToPsi.map(_.swap)
}
