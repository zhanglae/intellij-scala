package org.jetbrains.sbt
package project.structure

import java.io.{FileNotFoundException, _}
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.externalSystem.model.task.{ExternalSystemTaskId, ExternalSystemTaskNotificationEvent, ExternalSystemTaskNotificationListener}
import org.jetbrains.sbt.SbtUtil._
import org.jetbrains.sbt.project.structure.SbtRunner._
import org.jetbrains.sbt.shell.SbtShellCommunication
import org.jetbrains.sbt.shell.SbtShellCommunication.{EventAggregator, Output, TaskComplete, TaskStart}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Try}
import scala.xml.{Elem, XML}

/**
 * @author Pavel Fatin
 */
class SbtRunner(vmExecutable: File, vmOptions: Seq[String], environment: Map[String, String],
                customLauncher: Option[File], customStructureJar: Option[File],
                id: ExternalSystemTaskId,
                listener: ExternalSystemTaskNotificationListener) {
  private val LauncherDir = getSbtLauncherDir
  private val SbtLauncher = customLauncher.getOrElse(getDefaultLauncher)
  private def sbtStructureJar(sbtVersion: String) = customStructureJar.getOrElse(LauncherDir / s"sbt-structure-$sbtVersion.jar")

  private val cancellationFlag: AtomicBoolean = new AtomicBoolean(false)

  def cancel(): Unit =
    cancellationFlag.set(true)

  def read(directory: File, options: Seq[String], importFromShell: Boolean): Try[(Elem, String)] = {

    if (SbtLauncher.exists()) {

      val sbtVersion = detectSbtVersion(directory, SbtLauncher)
      val majorSbtVersion = majorVersion(sbtVersion)
      val useShellImport = importFromShell && shellImportSupported(sbtVersion)

      if (importSupported(sbtVersion)) usingTempFile("sbt-structure", Some(".xml")) { structureFile =>

        val messageResult: Try[String] =
          if (useShellImport) dumpFromShell(structureFile, options)
          else runDumpProcess(directory, structureFile, options: Seq[String], majorSbtVersion)

        messageResult.flatMap { messages =>
          if (structureFile.length > 0) Try {
            val elem = XML.load(structureFile.toURI.toURL)
            (elem, messages)
          }
          else Failure(SbtException.fromSbtLog(messages))
        }

      } else {
        val message = s"SBT $sinceSbtVersion+ required. Please update project build.properties."
        Failure(new UnsupportedOperationException(message))
      }
    }
    else {
      val error = s"SBT launcher not found at ${SbtLauncher.getCanonicalPath}"
      Failure(new FileNotFoundException(error))
    }
  }

  private val statusUpdate = (message:String) =>
    listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, message.trim))

  private def dumpFromShell(structureFile: File, options: Seq[String]): Try[String] = {
    val project = id.findProject()
    val shell = SbtShellCommunication.forProject(project)

    val fileString = structureFile.getAbsolutePath
    val optString = options.mkString(" ")
    val cmd = s";reload; */*:dumpStructureTo $fileString $optString"
    val output =
      shell.command(cmd, new StringBuilder, messageAggregator(id, statusUpdate), showShell = true)

    Await.ready(output, Duration.Inf)
    output.value.get.map(_.toString())
  }

  /** Aggregates (messages, warnings) and updates external system listener. */
  private def messageAggregator(id: ExternalSystemTaskId, statusUpdate: String=>Unit): EventAggregator[StringBuilder] = {
    case (m,TaskStart) => m
    case (m,TaskComplete) => m
    case (messages, Output(message)) =>
      statusUpdate(message)
      messages.append("\n").append(message)
      messages
  }

  private def shellImportSupported(sbtVersion: String): Boolean =
    versionCompare(sbtVersion, sinceSbtVersionShell) >= 0
  
  private def importSupported(sbtVersion: String): Boolean =
    versionCompare(sbtVersion, sinceSbtVersion) >= 0

  private def runDumpProcess(directory: File, structureFile: File, options: Seq[String], sbtVersion: String): Try[String] = {

    val optString = options.mkString(", ")

    val pluginJar = sbtStructureJar(sbtVersion)

    val setCommands = Seq(
      s"""shellPrompt := { _ => "" }""",
      s"""SettingKey[_root_.scala.Option[_root_.sbt.File]]("sbt-structure-output-file") in _root_.sbt.Global := _root_.scala.Some(_root_.sbt.file("${path(structureFile)}"))""",
      s"""SettingKey[_root_.java.lang.String]("sbt-structure-options") in _root_.sbt.Global := "$optString""""
    ).mkString("set _root_.scala.collection.Seq(", ",", ")")

    val sbtCommands = Seq(
      setCommands,
      s"""apply -cp "${path(pluginJar)}" org.jetbrains.sbt.CreateTasks""",
      "*/*:dump-structure",
      "exit"
    ).mkString(";",";","")

    val processCommandsRaw =
      path(vmExecutable) +:
        "-Djline.terminal=jline.UnsupportedTerminal" +:
        "-Dsbt.log.noformat=true" +:
        "-Dfile.encoding=UTF-8" +:
        (vmOptions ++ SbtOpts.loadFrom(directory)) :+
        "-jar" :+
        path(SbtLauncher)

    val processCommands = processCommandsRaw.filterNot(_.isEmpty)

    Try {
      val processBuilder = new ProcessBuilder(processCommands.asJava)
      processBuilder.directory(directory)
      processBuilder.environment().putAll(environment.asJava)
      val process = processBuilder.start()
      val result = using(new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream, "UTF-8")))) { writer =>
        writer.println(sbtCommands)
        writer.flush()
        handle(process, statusUpdate)
      }
      result.getOrElse("no output from sbt shell process available")
    }.orElse(Failure(SbtRunner.ImportCancelledException))
  }

  private def handle(process: Process, statusUpdate: String=>Unit): Try[String] = {
    val output = StringBuilder.newBuilder

    val processListener: (OutputType, String) => Unit = {
      case (OutputType.StdOut, text) =>
        if (text.contains("(q)uit")) {
          val writer = new PrintWriter(process.getOutputStream)
          writer.println("q")
          writer.close()
        } else {
          output.append(text)
          statusUpdate(text)
        }
      case (OutputType.StdErr, text) =>
        output.append(text)
        statusUpdate(text)
    }

    Try {
      val handler = new OSProcessHandler(process, "SBT import", Charset.forName("UTF-8"))
      handler.addProcessListener(new ListenerAdapter(processListener))
      handler.startNotify()

      var processEnded = false
      while (!processEnded && !cancellationFlag.get())
        processEnded = handler.waitFor(SBT_PROCESS_CHECK_TIMEOUT_MSEC)

      if (!processEnded) {
        // task was cancelled
        handler.setShouldDestroyProcessRecursively(false)
        handler.destroyProcess()
        throw ImportCancelledException
      } else output.toString()
    }
  }

  private def path(file: File): String = file.getAbsolutePath.replace('\\', '/')
}

object SbtRunner {
  case object ImportCancelledException extends Exception

  val SBT_PROCESS_CHECK_TIMEOUT_MSEC = 100

  def getSbtLauncherDir: File = {
    val file: File = jarWith[this.type]
    val deep = if (file.getName == "classes") 1 else 2
    (file << deep) / "launcher"
  }

  def getDefaultLauncher: File = getSbtLauncherDir / "sbt-launch.jar"

  private val sinceSbtVersion = "0.12.4"
  private val sinceSbtVersionShell = "0.13.5"

}
