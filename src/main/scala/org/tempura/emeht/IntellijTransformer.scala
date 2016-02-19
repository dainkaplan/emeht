package org.tempura.emeht

object HexToRGBOps {
  def handle6(str: String) =
    for (cc <- str.sliding(2,2).toSeq) yield java.lang.Short.valueOf(cc, 16).toShort
  def handle3(str: String) =
    for (c <- str) yield java.lang.Short.valueOf(c.toString * 2, 16).toShort

  implicit class ToHexStr(int: Int) {
    def toHexStr = "%02x".format(int)
  }

  val hexDigits = Set() ++ ('0' to '9') ++ ('a' to 'f') ++ ('A' to 'F')

  def notHex(digits: String): Boolean =
    digits.exists(c => !hexDigits.contains(c))

  def hexToRGB(str: String): Seq[Short] = {
    val trimmed = if (str startsWith "#") str.drop(1) else str
    if (trimmed.length > 6 || notHex(trimmed)) List()
    else handle6(("0" * (6 - trimmed.length)) + trimmed)
  }

  def invert(in: String): String = hexToRGB(in) match {
    case List() => in
    case converted => (for {
      s <- hexToRGB(in)
      i = 0xFF - s // Actually invert the code
    } yield i.toHexStr).mkString
  }
}

object IntellijTransformer {
  // add an implicit conversion from Transform -> NodeSeq
  import org.fusesource.scalate.scuery.Transformer

  def isColorName(name: String): Boolean = {
    name == "FOREGROUND" || name == "BACKGROUND" || (name endsWith "_COLOR")
  }

  object transformer extends Transformer {
    $("scheme colors option").attribute("value") { n =>
      (for {
        attrs <- n.attribute("value")
        attr <- attrs.headOption
      } yield HexToRGBOps.invert(attr.text)).getOrElse("")
    }
    $("scheme attributes option value option").attribute("value") { n =>
      //println(n)
      (for {
        attrs <- n.attribute("value")
        attr <- attrs.headOption
        name = n.attribute("name").exists(_.map(_.text).exists(isColorName))
      } yield name match {
        case true => HexToRGBOps.invert(attr.text)
        case false => attr.text
      }).getOrElse("")
    }
  }
  object schemeTransformer extends Transformer {
        $("scheme") { n =>
          val name = (for {
            attrs <- n.attribute("name")
            attr <- attrs.headOption
          } yield attr.text).get
          val newnameAttr = scala.xml.Attribute(None, "name", scala.xml.Text(name + " (emeht)"), scala.xml.Null)
          n.asInstanceOf[scala.xml.Elem] % newnameAttr
        }
  }

  def apply(xml: scala.xml.NodeSeq): scala.xml.Node = schemeTransformer(transformer(xml)).head
}

object IntellijColorSettingsInverter {
  import scala.xml._
  import java.io.FileWriter
  import FileOps._

  def invertColors(xmlInput: String): String = {
    val xml = XML.loadString(xmlInput)
    val xml2 = IntellijTransformer(xml)
    new PrettyPrinter(200, 2).format(xml2)
  }

  // Read jar, for each file in jar/colors, if .xml or .icls, transform, then jar up and save.
  def invertColorsForSettingsJar(jarFile: java.io.File, cleanUpAfter: Boolean = true): Unit = {
    def validThemeFile(file: java.io.File): Boolean =
      file.getName.endsWith(".xml") || file.getName.endsWith(".icls")

    val dest = JarUtils.extractJar(jarFile, deleteFirst = true)
    val colorDir = new java.io.File(dest, "colors")
    if (!colorDir.exists()) {
      println("No color settings found?")
    } else {
      for {
        f <- colorDir.listFiles()
        if validThemeFile(f)
        xml = scala.io.Source.fromFile(f).getLines().mkString
        _ = println("Inverting colors for " + f.getName)
        xml2 = invertColors(xml)
      } {
        val writer = new FileWriter(f.withName(_ + " (emeht)"))
        writer.write(xml2)
        writer.close()
        f.delete()
      }
      val targetFile = jarFile.withName(_ + ".emeht").withParent(null)
      println(s"Jarring to ./${targetFile.getName}...")
      if (targetFile.exists()) targetFile.delete()
      JarUtils.makeJar(targetFile)(dest)
    }
    if (cleanUpAfter) {
      println("Deleting temporary files...")
      dest.deleteAll()
    }
  }
}