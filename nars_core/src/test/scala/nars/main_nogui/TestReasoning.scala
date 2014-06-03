package nars.main_nogui

import org.junit.Assert.assertTrue
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.List
import org.junit.Test
import TestReasoning._
//remove if not needed
import scala.collection.JavaConversions._

object TestReasoning {

  private val IN_TXT = "-in.txt"

  private val OUT_TXT = "-out.txt"

  def main(args: Array[String]) {
    val testReasoning = new TestReasoning()
    if (args.length == 1) {
      testReasoning.checkReasoning(new File(args(0)))
    } else {
      testReasoning.testExamples()
    }
  }
}

/**
 Unit Test Reasoning, using input and output files from nars-dist/Examples ;
 * <pre>
 * To create a new test input, add the NARS input as XX-in.txt in nars-dist/Examples ,
 *  run the test suite, and move resulting file in temporary directory
 * /tmp/nars_test/XX-out.txt
 * into nars-dist/Example
 * </pre>
 *
 */
class TestReasoning {

  private var tmpDir: File = new File(tmpDir_, "nars_test")

  val tmpDir_ = System.getProperty("java.io.tmpdir")

  tmpDir.mkdir()

  println("TestReasoning: tests results are in " + tmpDir)

  @Test
  def testExamples() {
    val testDir_ = "../nars-dist/Examples"
    val testDir = new File(testDir_)
    val allFiles = testDir.listFiles()
    var testPassed = true
    for (i <- 0 until allFiles.length) {
      val file = allFiles(i)
      if (file.getName.contains(IN_TXT)
		&& ! file.isHidden() ) {
        println("Test file " + file)
        testPassed &= checkReasoning(file)
      }
    }
    assertTrue("compared Results", testPassed)
  }

  /**
   run reasoning and check results
   */
  private def checkReasoning(file: File): Boolean = {
    try {
      val nars = new NARSBatch()
      val resultFile = new File(tmpDir, file.getName.replace(IN_TXT, OUT_TXT))
      nars.setPrintStream(new PrintStream(resultFile))
      nars.runInference(Array(file.getAbsolutePath))
      return compareResult(file, resultFile)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    false
  }

  private def compareResult(file: File, resultFile: File): Boolean = {
    var comparison = true
    try {
      val referenceFile_ = file.getAbsolutePath.replace(IN_TXT, OUT_TXT)
      val referenceFile = new File(referenceFile_)
      val referenceLines = Files.readAllLines(referenceFile.toPath(), Charset.forName("UTF-8"))
      val actualLines = Files.readAllLines(resultFile.toPath(), Charset.forName("UTF-8"))
      var i = 0
      for (referenceLine <- referenceLines) {
        if (i < actualLines.size) {
          val al = actualLines.get(i)
          if (referenceLine != al) {
            println(al)
            println("DIFFERS from reference (l " + (i + 1) + "):")
            println(referenceLine)
            comparison = false
          }
        } else {
          println("Actual result is not long enough: line " + i + " / " + 
            referenceLines.size)
          comparison = false
          //break
        }
        i += 1
      }
      println("Finished comparison for file:" + file + " line " + i)
    } catch {
      case e: IOException => {
        e.printStackTrace()
        return false
      }
    }
    comparison
  }
}
