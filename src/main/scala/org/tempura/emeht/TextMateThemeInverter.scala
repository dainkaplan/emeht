package org.tempura.emeht

// Blindly replace all
//   <string>#1D1F21</string>
// with inverted colours; if doesn't start with # then ignore value.
object TextMateThemeInverter {
  import java.io.FileWriter
  import FileOps._

  val regex = """<string>(#[0-9A-Fa-f]{6})</string>""".r
  val nameRegex = """<key>name</key>([\s\n\r]+)<string>(.*?)</string>""".r

  def invertColors(xmlInput: String): String = {
    def invertHex(in: String): String = "<string>" + ColorOps.invert(in) + "</string>"
    def replaceName(spaces: String, name: String): String = s"<key>name</key>$spaces<string>$name (emeht)</string>"
    val fixedColors = regex.replaceAllIn(xmlInput, f => invertHex(f.group(1)))
    val fixedName = nameRegex.replaceAllIn(fixedColors, f => replaceName(f.group(1), f.group(2)))
    fixedName
  }

  def invertColorsForTmTheme(file: java.io.File): Unit = {
    def validThemeFile(file: java.io.File): Boolean =
      file.getName.endsWith(".tmTheme")

    if (validThemeFile(file)) {
      val xml = scala.io.Source.fromFile(file).getLines().mkString("\n")
      println("Inverting colors for " + file.getName)
      val newfile = file.withName(_ + ".emeht").withParent(null)
      if (newfile.exists()) newfile.delete()
      val writer = new FileWriter(newfile)
      writer.write(invertColors(xml))
      writer.close()
    }
  }
}
