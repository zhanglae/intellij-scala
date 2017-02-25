package org.jetbrains.plugins.scala.components.extensions.api.impl

import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.libraries.Library
import org.jetbrains.plugins.scala.components.extensions.api._
import org.jetbrains.plugins.scala.components.extensions.api.base._


/**
  * @author mutcianm
  * @since 21.02.17.
  */
class IdeaApiProvider(project: Project) extends BaseApiProvider {
  override def version = 1

  override val getProject = new IdeaProject {
    override def name = project.getName
    override def modules = ModuleManager.getInstance(project).getModules.map(new IdeaModuleImpl(_))
  }

  class IdeaModuleImpl(module: Module) extends IdeaModule {
    override def name = module.getName
    override def libraries = ModuleRootManager.getInstance(module).getModifiableModel.getModuleLibraryTable.getLibraries.map(new LibraryDependencyImpl(_))
    override def compilerSettings = new ComilerSettingsImpl(module)
  }

  class LibraryDependencyImpl(lib: Library) extends LibraryDependency {
    override def name = lib.getName
    override def version = LibraryVersion(name)
  }

  class ComilerSettingsImpl(module: Module) extends CompilerSettings {
    import org.jetbrains.plugins.scala.project._
    override def compilerOptions() = module.scalaCompilerSettings.toOptions
    override def compilerPlugins() = module.scalaCompilerSettings.plugins
  }
}
