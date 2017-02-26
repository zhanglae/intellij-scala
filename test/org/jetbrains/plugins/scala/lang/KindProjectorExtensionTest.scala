package org.jetbrains.plugins.scala.lang

import org.jetbrains.plugins.scala.lang.types.kindProjector.KindProjectorTestBase

/**
  * @author mutcianm
  * @since 26.02.17.
  */
class KindProjectorExtensionTest extends KindProjectorTestBase {
  def testInlineSyntax() = doTest(
    """
      |def foo: /*start*/Either[Int, +?]/*end*/ = ???
      |//({ type Λ[+β] = Either[Int, β] })#Λ
    """.stripMargin
  )
}
