package org.tempura.emeht

import java.util.zip.{ZipEntry, ZipOutputStream}

import scala.io.Source

// Taken from: https://www.artima.com/forums/flat.jsp?forum=283&thread=245068
object JarUtils {

  import java.io._
  import java.util.jar._
  import FileOps._

  def copyStream(istream: InputStream, ostream: OutputStream): Unit = {
    val bytes = new Array[Byte](1024)
    var len = -1
    while ( {len = istream.read(bytes, 0, 1024); len != -1})
      ostream.write(bytes, 0, len)
  }

  def extractJar(file: File, deleteFirst: Boolean = false, debug: Boolean = false): File = {
    val basename = file.getName.substring(0, file.getName.lastIndexOf("."))
    val todir = new File(file.getParentFile, basename)
    if (deleteFirst && todir.exists()) todir.deleteAll()
    todir.mkdirs()

    if (debug) println(s"Extracting ${file.getName} to $todir")
    val jar = new JarFile(file)
    val enu = jar.entries
    while (enu.hasMoreElements) {
      val entry = enu.nextElement
      val entryPath =
        if (entry.getName.startsWith(basename)) entry.getName.substring(basename.length)
        else entry.getName

      if (debug) println(s"Extracting to $todir/$entryPath")
      if (entry.isDirectory) {
        new File(todir, entryPath).mkdirs()
      } else {
        val istream = jar.getInputStream(entry)
        val entryFile = new File(todir, entryPath)
        if (!entryFile.getParentFile.exists())
          entryFile.getParentFile.mkdirs()
        val ostream = new FileOutputStream(entryFile)
        copyStream(istream, ostream)
        ostream.close()
        istream.close()
      }
    }
    todir
  }

  // Mostly from: http://stackoverflow.com/questions/9985684/how-do-i-archive-multiple-files-into-a-zip-file-using-scala
  def makeJar(outputFile: File)(root: File) {
    def readByte(bufferedReader: BufferedReader): Stream[Int] =
      bufferedReader.read() #:: readByte(bufferedReader)
    val zip = new ZipOutputStream(new FileOutputStream(outputFile))
    try {
      def relativeName(file: File): String = {
        file.getAbsolutePath.substring(root.getAbsolutePath.length)
      }
      for (file <- root.allFilesRecursive) if (file.isDirectory) {
        zip.putNextEntry(new ZipEntry(relativeName(file) + "/"))
      } else {
        zip.putNextEntry(new ZipEntry(relativeName(file)))
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