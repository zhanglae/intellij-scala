package org.jetbrains.plugins.scala.components.extensions.api.base

/**
  * User: Dmitry.Naydanov
  * Date: 16.02.17.
  */
trait MigrationDescriptor {
  def allFrom: Iterable[LibraryDependency]
  def allTo: Iterable[LibraryDependency]
}
