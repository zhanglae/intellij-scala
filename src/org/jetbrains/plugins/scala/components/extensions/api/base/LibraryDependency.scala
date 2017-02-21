package org.jetbrains.plugins.scala.components.extensions.api.base

/**
  * User: Dmitry.Naydanov
  * Date: 16.02.17.
  */
trait LibraryDependency {
  def name: String
  def version: LibraryVersion
}
