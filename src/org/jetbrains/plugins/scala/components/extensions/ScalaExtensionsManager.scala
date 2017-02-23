package org.jetbrains.plugins.scala.components.extensions

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.components.extensions.api.ArbitraryTreeTransformer
import org.jetbrains.plugins.scala.components.extensions.api.impl.IdeaApiProvider
import org.jetbrains.plugins.scala.components.extensions.impl.KindProjectorExtension
import org.jetbrains.plugins.scala.lang.parser.parsing
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder
import org.jetbrains.plugins.scala.lang.psi.{ScalaPsiElement, ScalaPsiElementImpl}
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScParameterizedTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScMethodCall}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScObject, ScTrait}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.impl.base.types.ScParameterizedTypeElementImpl
import org.jetbrains.plugins.scala.lang.psi.impl.expr.ScMethodCallImpl
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.{ScClassImpl, ScObjectImpl, ScTraitImpl}

import scala.collection.mutable
import scala.meta._
import scala.meta.trees.TreeConverter
import scala.reflect.ClassTag

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

  def transformElement[T <: ScalaPsiElement](elem: T)(implicit tag: ClassTag[T])= {
    val transformers = getTransformersForElement(elem)
    val converted = converter.ideaToMeta(elem)
    val result = transformers.foldLeft(Option(converted))((res, transformer) => res.flatMap(transformer.transform))
    val parse = psiToParse.getOrElse(tag.runtimeClass.asSubclass(classOf[ScalaPsiElement]), parsing.CompilationUnit.parse(_))
    result.map(t =>
      ScalaPsiElementFactory.createElementWithContext(t.toString(), elem.getContext, elem, parse)
    ).getOrElse(elem)
  }

  private def loadExtensions(): Unit = {
    for { extension <- getExtensions
          tr <- extension.transformers }
    {
      transformersByContext(tr.context.elementType) = transformersByContext.getOrElse(tr.context.elementType, Seq.empty) :+ tr
    }
  }

  private val converter = new TreeConverter {
    override def getCurrentProject: Project = project
    override def dumbMode: Boolean = true
  }

  private val transformersByContext: mutable.Map[Class[_ <: Tree], Seq[ArbitraryTreeTransformer]] = mutable.Map.empty

}

object ScalaExtensionsManager {
  def getInstance(project: Project): ScalaExtensionsManager = project.getComponent(classOf[ScalaExtensionsManager])

  val metaToPsi: Map[Class[_ <: Tree], Class[_ <: ScalaPsiElement]] = Map(
    classOf[Tree]           -> classOf[ScalaPsiElementImpl],
    classOf[Term]           -> classOf[ScExpression],
    classOf[Defn.Class]     -> classOf[ScClassImpl],
    classOf[Defn.Trait]     -> classOf[ScTraitImpl],
    classOf[Defn.Object]    -> classOf[ScObjectImpl],
    classOf[Term.Apply]     -> classOf[ScMethodCallImpl],
    classOf[Term.ApplyType] -> classOf[ScParameterizedTypeElementImpl]
  )

  val psiToMeta: Map[Class[_ <: ScalaPsiElement], Class[_ <: Tree]] = metaToPsi.map(_.swap)

  val psiToParse: Map[Class[_ <: ScalaPsiElement], ScalaPsiBuilder => AnyVal] = Map(
    classOf[ScalaPsiElementImpl]  -> parsing.CompilationUnit.parse,
    classOf[ScExpression]         -> parsing.expressions.Expr.parse,
    classOf[ScParameterizedTypeElementImpl] -> (x => parsing.types.Type.parse(x)),
    classOf[ScClassImpl]          -> parsing.top.ClassDef.parse,
    classOf[ScTraitImpl]          -> parsing.top.TraitDef.parse,
    classOf[ScObjectImpl]         -> parsing.top.ObjectDef.parse,
    classOf[ScMethodCallImpl]     -> parsing.expressions.SimpleExpr.parse,
  )
}
