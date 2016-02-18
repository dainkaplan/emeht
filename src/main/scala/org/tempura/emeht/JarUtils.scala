package org.tempura.emeht

import java.util.zip.{ZipEntry, ZipOutputStream}

import scala.io.Source

// Taken from: https://www.artima.com/forums/flat.jsp?forum=283&thread=245068
object JarUtils {

  import java.io._
  import java.util.jar._

  def copyStream(istream: InputStream, ostream: OutputStream): Unit = {
    val bytes = new Array[Byte](1024)
    var len = -1
    while ( {len = istream.read(bytes, 0, 1024); len != -1})
      ostream.write(bytes, 0, len)
  }

  def extractJar(file: File): File = {
    val basename = file.getName.substring(0, file.getName.lastIndexOf("."))
    val todir = new File(file.getParentFile, basename)
    todir.mkdirs()

    println("Extracting " + file + " to " + todir)
    val jar = new JarFile(file)
    val enu = jar.entries
    while (enu.hasMoreElements) {
      val entry = enu.nextElement
      val entryPath =
        if (entry.getName.startsWith(basename)) entry.getName.substring(basename.length)
        else entry.getName

      println("Extracting to " + todir + "/" + entryPath)
      if (entry.isDirectory) {
        new File(todir, entryPath).mkdirs
      } else {
        val istream = jar.getInputStream(entry)
        val ostream = new FileOutputStream(new File(todir, entryPath))
        copyStream(istream, ostream)
        ostream.close()
        istream.close()
      }
    }
    todir
  }

  // From: http://stackoverflow.com/questions/9985684/how-do-i-archive-multiple-files-into-a-zip-file-using-scala
  def makeJar(zipFilepath: String)(files: File*) {
    def readByte(bufferedReader: BufferedReader): Stream[Int] =
      bufferedReader.read() #:: readByte(bufferedReader)
    val zip = new ZipOutputStream(new FileOutputStream(zipFilepath))
    try {
      for (file <- files) {
        zip.putNextEntry(new ZipEntry(file.getName))
        val in = Source.fromFile(file.getCanonicalPath).bufferedReader()
        try {
          readByte(in).takeWhile(_ > -1).toList.foreach(zip.write)
        } finally {
          in.close()
        }
        zip.closeEntry()
      }
    } finally {
      zip.close()
    }
  }
}