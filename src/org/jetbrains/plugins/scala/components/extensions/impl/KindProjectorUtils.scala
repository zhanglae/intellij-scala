package org.jetbrains.plugins.scala.components.extensions.impl
import scala.meta.Type

/**
  * @author mutcianm
  * @since 02.03.17.
  */
trait KindProjectorUtils {
  import scala.meta._

  protected def generateName(i: Int): String = {
    //kind projector generates names the same way
    val res = ('α' + (i % 25)).toChar.toString
    if (i < 25) res
    else res + (i / 25)
  }

  protected def toTParam(tp: Type): Type.Param = tp match {
    case t: Type.Placeholder => tparam"_"
    case n: Type.Name => Type.Param(Nil, n, Nil, Type.Bounds(None, None), Nil, Nil)
    case Type.Apply(n: Type.Name, targs) => Type.Param(Nil, n, targs.map(toTParam), Type.Bounds(None, None), Nil, Nil)
  }
}
