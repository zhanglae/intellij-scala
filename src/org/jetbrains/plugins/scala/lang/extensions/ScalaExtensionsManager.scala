package org.jetbrains.plugins.scala.lang.extensions

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScTrait}

import scala.meta._

class ScalaExtensionsManager(project: Project) extends ProjectComponent {
  import ScalaExtensionsManager._

  override def projectOpened(): Unit = ()
  override def projectClosed(): Unit = ()
  override def initComponent(): Unit = ()
  override def disposeComponent(): Unit = ()
  override def getComponentName: String = "ScalaExtensionsManager"

  def getExtensions: Seq[ScalaPluginExtension] = ???
  def getApplicableExtensions(elem: ScalaPsiElement): Seq[ScalaPluginExtension] = getExtensions

}

object ScalaExtensionsManager {
  def getInstance(project: Project): ScalaExtensionsManager = project.getComponent(classOf[ScalaExtensionsManager])

  val metaToPsi: Map[Class[_ <: Tree], Class[_ <: ScalaPsiElement]] = Map(
    classOf[Tree] -> classOf[ScalaPsiElement],
    classOf[Term] -> classOf[ScExpression],
    classOf[Defn.Class] -> classOf[ScClass],
    classOf[Defn.Trait] -> classOf[ScTrait],
//    classOf[]
  )
}
