package org.jetbrains.plugins.scala.lang.extensions.api

import org.jetbrains.plugins.scala.lang.extensions.api.base.IdeaProject

/**
  * @author mutcianm
  * @since 09.02.17.
  */
abstract class BaseApiProvider {
  def version: Int
  
  def getProject: IdeaProject
}
