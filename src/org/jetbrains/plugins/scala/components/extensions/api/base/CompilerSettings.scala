package org.jetbrains.plugins.scala.components.extensions.api.base

/**
  * @author mutcianm
  * @since 24.02.17.
  */
trait CompilerSettings {
//  def compilerVersion(): String
  def compilerOptions(): Seq[String]
  def compilerPlugins(): Seq[String]
}
