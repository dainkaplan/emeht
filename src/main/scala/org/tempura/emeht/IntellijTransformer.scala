package org.tempura.emeht

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
      } yield ColorOps.invert(attr.text)).getOrElse("")
    }
    $("scheme attributes option value option").attribute("value") { n =>
      //println(n)
      (for {
        attrs <- n.attribute("value")
        attr <- attrs.headOption
        name = n.attribute("name").exists(_.map(_.text).exists(isColorName))
      } yield name match {
        case true => ColorOps.invert(attr.text)
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