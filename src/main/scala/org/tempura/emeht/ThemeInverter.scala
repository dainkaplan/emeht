package org.tempura.emeht

import java.io.FileWriter

import org.fusesource.scalate.scuery.Transform

import scala.xml.Null

object HexToRGBOps {
  def handle6(str: String) =
    for (cc <- str.sliding(2,2).toSeq) yield java.lang.Short.valueOf(cc, 16).toShort
  def handle3(str: String) =
    for (c <- str) yield java.lang.Short.valueOf(c.toString * 2, 16).toShort

  val hexDigits = Set() ++ ('0' to '9') ++ ('a' to 'f') ++ ('A' to 'F')

  def hexToRGB(str: String): Seq[Short] = {
    val trimmed = if (str startsWith "#") str.drop(1) else str
    trimmed match {
      case t if t.length <= 6 && t.forall(hexDigits.contains) =>
        handle6( ("0" * (6 - t.length)) + t)
      case o => List()
    }
  }

  def invert(hexCode: String): String = {
    val inverted = hexToRGB(hexCode) match {
      case List() => hexCode
      case converted => (for {
        s <- hexToRGB(hexCode)
        i = 0xFF - s
      } yield "%02x".format(i)).mkString
    }
    //println(s"'$hexCode' to '$inverted'")
    inverted
  }
}

object ThemeInverter {
  // add an implicit conversion from Transform -> NodeSeq
  import org.fusesource.scalate.scuery.Transformer

  def isColorName(name: String): Boolean = {
    name == "FOREGROUND" || name == "BACKGROUND" || (name endsWith "_COLOR")
  }

  object transformer extends Transformer {
    $("scheme") { n =>
      val name = (for {
        attrs <- n.attribute("name")
        attr <- attrs.headOption
      } yield attr.text).get
      val newnameAttr = scala.xml.Attribute(None, "name", scala.xml.Text(name + " (emeht)"), scala.xml.Null)
      n.asInstanceOf[scala.xml.Elem] % newnameAttr
    }
    $("scheme colors option").attribute("value") { n =>
      (for {
        attrs <- n.attribute("value")
        attr <- attrs.headOption
      } yield HexToRGBOps.invert(attr.text)).get
    }
    $("scheme attributes option value option").attribute("value") { n =>
      (for {
        attrs <- n.attribute("value")
        attr <- attrs.headOption
        name = n.attribute("name").exists(_.map(_.text).exists(isColorName))
      } yield name match {
        case true => HexToRGBOps.invert(attr.text)
        case false => attr.text
      }).get
    }
  }

  def apply(xml: scala.xml.NodeSeq): scala.xml.Node = transformer(xml).head
}

object InvertColors {
  import scala.xml._
  def invertColors(xmlInput: String): String = {
    val xml = XML.loadString(xmlInput)
    val xml2 = ThemeInverter(xml)
    new PrettyPrinter(200, 2).format(xml2)
  }

  // Read jar, for each file in jar/colors, if .xml or .icls, transform, then jar up and save.
  def invertColorsForSettingsJar(jarFile: java.io.File): Unit = {

    def validThemeFile(file: java.io.File): Boolean = {
      file.getName.endsWith(".xml") || file.getName.endsWith(".icls")
    }

    def withFilename(file: java.io.File, withPath: Boolean = false)(f: String => String): String = {
      val fullpath = file.getAbsolutePath
      val basepath = fullpath.substring(0, fullpath.lastIndexOf("/") + 1)
      val name = file.getName
      val noext = name.substring(0, name.lastIndexOf("."))
      val ext = name.substring(name.lastIndexOf("."))
      (if (withPath) basepath else "") + f(noext) + ext
    }

    def allFiles(f: java.io.File): List[java.io.File] = f match {
      case _ if f.isDirectory => f.listFiles().toList.flatMap(allFiles)
      case _ => List(f)
    }

    val dest = JarUtils.extractJar(jarFile)
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
        val writer = new FileWriter(withFilename(f, true)(_ + " (emeht)"))
        writer.write(xml2)
        writer.close()
        f.delete()
      }
      println("Jarring...")
      JarUtils.makeJar(withFilename(jarFile)(_ + ".emeht"))(allFiles(dest): _*)
    }
    //println("Deleting tmp...")
    //dest.delete()
  }
}

object Runner extends App {
  assert(args.length == 1, "Needs one argument, the filename of the jar whose colors should be inverted.")
  InvertColors.invertColorsForSettingsJar(new java.io.File(args(0)))
}