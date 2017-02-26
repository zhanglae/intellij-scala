package org.jetbrains.plugins.scala.components.extensions.impl

import org.jetbrains.plugins.scala.components.extensions.ScalaPluginExtension
import org.jetbrains.plugins.scala.components.extensions.api._

import scala.meta._
import scala.collection.immutable.Seq
import scala.{Seq => _}

/**
  * @author mutcianm
  * @since 21.02.17.
  */
class KindProjectorExtension(api: BaseApiProvider) extends ScalaPluginExtension(api) {
  override def extensionName = "KindProjectorExtension"
  override def extensionDescription = "Support for kind projector syntax in IntelliJ IDEA"

//  override def transformers = Seq(new TypeElementRewriter)

  class TypeElementRewriter extends ArbitraryTreeTransformer {
    override def name = "KindProjector TypeElementRewriter"
    override def description = "KindProjector TypeElementRewriter description"
    private val inlineSyntaxIds = Set("?", "+?", "-?")
    private val functionSyntaxIds = Set("Lambda", "λ")

    private def isKindProjectorEnabled = api.getProject.modules.exists(_.compilerSettings.compilerPlugins().exists(_.contains("kind-proj")))

    private def isKindProjectorInlineSyntax(tp: Tree): Boolean = tp match {
      case Type.Name(value)         =>
        inlineSyntaxIds.contains(value)
      case Type.Apply(tname, tparams)    =>
        tparams.exists(isKindProjectorInlineSyntax) ||
          isKindProjectorInlineSyntax(tname)
      case _                        =>
        false
    }

    private def isKindProjectorFunctionSyntax(tp: Tree): Boolean = tp match {
      case t"${Type.Name(value)}[$_]"   => functionSyntaxIds.contains(value)
      case _                            => false
    }

    private def generateName(i: Int): String = {
      //kind projector generates names the same way
      val res = ('α' + (i % 25)).toChar.toString
      if (i < 25) res
      else res + (i / 25)
    }

    private def toTParam(tp: Type): Type.Param = tp match {
      case t:Type.Placeholder => tparam"_"
      case n:Type.Name        => Type.Param(Nil, n, Nil, Type.Bounds(None, None), Nil, Nil)
      case Type.Apply(n:Type.Name, targs) => Type.Param(Nil, n, targs.map(toTParam), Type.Bounds(None, None), Nil, Nil)
    }

    private def transformName(tp: Type, index: Int): Type.Param = tp match {
      case Type.Apply(Type.Name(value), paramss) =>
        toTParam(Type.Apply(Type.Name(value.replace("?", generateName(index))), paramss))
      case Type.Name(value) =>
        toTParam(Type.Name(value.replace("?", generateName(index))))
    }

    private def mkInlineSyntax(tree: Tree): Tree = {
      val t"$tname[..$paramss]" = tree
      Type.Apply(tname, Seq(Type.Apply(tname, Seq(tname))))
      val (exported: Seq[Option[Type.Param]], defns: Seq[Type]) = paramss.zipWithIndex.map {
        case (tp@Type.Name(value), i) if inlineSyntaxIds.contains(value) =>
          (Some(transformName(tp, i)), Type.Name(generateName(i)))
        case (tp@Type.Apply(Type.Name(value), _), i) if inlineSyntaxIds.contains(value) =>
          (Some(transformName(tp, i)), Type.Name(generateName(i)))
        case (other, _) => (None, Type.Name(other.toString()))
      }.unzip
      t"({type Λ[..${exported.flatten}] = $tname[..$defns]})#Λ"
    }

    override def transform(tree: Tree): Option[Tree] = {
      val enabled = isKindProjectorEnabled
      val inlineSyn = isKindProjectorInlineSyntax(tree)
      val functionSyn = isKindProjectorFunctionSyntax(tree)
      if (enabled && inlineSyn) Some(mkInlineSyntax(tree))
      else None
    }


    override def additionalDeclarations(hint: Option[Tree] = None) = Seq(
      q"class Lambda",
      q"class λ",
      q"class ?",
      q"class +?",
      q"class -?"
    )

    override def context = ApplicabilityContext(classOf[Term.ApplyType])

  }
}
