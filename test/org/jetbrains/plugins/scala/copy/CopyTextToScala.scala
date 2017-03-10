package org.jetbrains.plugins.scala.copy

import org.jetbrains.plugins.scala.conversion.copy.plainText.TextJavaCopyPastePostProcessor.insideIde
import org.jetbrains.plugins.scala.settings.ScalaProjectSettings

/**
  * Created by Kate Ustuyzhanina on 12/28/16.
  */
class CopyTextToScala extends CopyTestBase() {
  override protected def doTest(fromText: String, toText: String, expectedText: String): Unit = {
    insideIde = false
    super.doTest(fromText, toText, expectedText)
    insideIde = true
  }

  override protected def setUp(): Unit = {
    super.setUp()
    ScalaProjectSettings.getInstance(getProject).setDontShowConversionDialog(true)
  }

  def testWrapWithExpression(): Unit = {
    val fromText = "<selection>new double[]{1.0, 2, 3};</selection>"

    val expected = "Array[Double](1.0, 2, 3)"

    doTestEmptyToFile(fromText, expected)
  }

  def testWrapWithFunction(): Unit = {
    val fromText =
      """
        |<selection>assert true : "Invocation of 'paste' operation for specific caret is not supported";</selection>
      """.stripMargin

    val expected =
      """
        |assert(true, "Invocation of 'paste' operation for specific caret is not supported")
      """.stripMargin
    doTestEmptyToFile(fromText, expected)
  }

  def testWrapWithClass(): Unit = {
    val fromText =
      """
        |<selection>public void doExecute() {
        |   assert true : "Invocation of 'paste' operation for specific caret is not supported";
        |}</selection>
      """.stripMargin

    val expected =
      """def doExecute(): Unit = {
        |  assert(true, "Invocation of 'paste' operation for specific caret is not supported")
        |}""".stripMargin

    doTestEmptyToFile(fromText, expected)
  }

  def testWrapWithClass2(): Unit = {
    val fromText =
      """
        |<selection>int i = 6;
        |boolean a = false;
        |String s = "false";</selection>
      """.stripMargin

    val expected =
      """
        |val i: Int = 6
        |val a: Boolean = false
        |val s: String = "false"
      """.stripMargin

    doTestEmptyToFile(fromText, expected)
  }

  def testAsFile(): Unit = {
    val fromText =
      """
        |<selection>import java.io.File;
        |public class Main {
        |	int func() {
        |		int qwe = 34;
        |		Boolean b = true;
        |		File f = new File("sdf");
        |		return 21;
        |	}
        |}</selection>
      """.stripMargin

    val expected =
      """import java.io.File
        |
        |class Main {
        |  def func: Int = {
        |    val qwe: Int = 34
        |    val b: Boolean = true
        |    val f: File = new File("sdf")
        |    21
        |  }
        |}""".stripMargin

    doTestEmptyToFile(fromText, expected)
  }

  def testJavaFileWithoutSemicolon(): Unit = {
    val fromText =
      """
        |<selection>class Test {
        |    public static void main(String[] args) {
        |        System.out.println("hello")
        |        System.out.println(" how are you?");
        |    }
        |}</selection>
      """.stripMargin

    val expected =
      """object Test {
        |  def main(args: Array[String]): Unit = {
        |    System.out.println("hello")
        |    System.out.println(" how are you?")
        |  }
        |}""".stripMargin

    doTestEmptyToFile(fromText, expected)
  }

  def testPrefixedExpression(): Unit = {
    val fromText ="""<selection>Arrays.asList("peter", "anna", "mike", "xenia");</selection>""".stripMargin
    val expected =
      """
        |import java.util
        |
        |util.Arrays.asList("peter", "anna", "mike", "xenia")
      """.stripMargin
    doTestEmptyToFile(fromText, expected)
  }

  def testJavaMethodWithoutLatestCurlyBrackets(): Unit = {
    val fromText =
      """
        |class Test {
        |    <selection>public static void main(String[] args) {
        |        System.out.println("hello")
        |        System.out.println(" how are you?"); </selection>
        |}
      """.stripMargin


    val expected =
      """def main(args: Array[String]): Unit = {
        |  System.out.println("hello")
        |  System.out.println(" how are you?")
        |}""".stripMargin
    doTestEmptyToFile(fromText, expected)
  }

  def testEmptyJavaClass(): Unit = {
    doTestEmptyToFile("<selection>public class Test {}</selection>", "class Test {}")
  }

  /** ****************** Valid scala code. No conversion expected. ******************/

  def testNoConversion1(): Unit = {
    doTestEmptyToFile("<selection>class Test {}</selection>", "class Test {}")
  }

  def testNoConversion2(): Unit = {
    doTestEmptyToFile("<selection>class Test extends Any</selection>", "class Test extends Any")
  }


  override val fromLangExtension: String = ".txt"
}
