package org.tempura.emeht

object FileOps {
  import java.io.File
  implicit class FileNames(file: File) {
    def withName(f: String => String, newPath: Option[File] = None): File = {
      val name = file.getName
      val noext = name.substring(0, name.lastIndexOf("."))
      val ext = name.substring(name.lastIndexOf("."))
      new File(
        newPath.getOrElse(file.getParentFile),
        f(noext) + ext)
    }
    def withParent(p: File): File = new File(p, file.getName)

    def allFilesRecursive: Iterator[java.io.File] = {
      // XXX: Not stack-safe
      def allFiles(f: java.io.File): Iterator[java.io.File] =
        List(f).toIterator ++ {
          if(f.isDirectory) f.listFiles().toIterator.flatMap(allFiles)
          else List()
        }
      allFiles(file)
    }

    def deleteAll(unsafeDelete: Boolean = false) = {
      // XXX: We only care about deleting within our current directory, so let's make this safer:
      if (!unsafeDelete) {
        if (!(file.getAbsolutePath startsWith new File("").getAbsolutePath))
          throw new Exception("You can only deleteAll() for subdirectories of pwd.")
      }

      // XXX: Not stack-safe
      def allFiles(f: java.io.File): Iterator[java.io.File] =
        List().toIterator ++ {
          if(f.isDirectory) f.listFiles().toIterator.flatMap(allFiles)
          else List()
        } ++ List(f).toIterator
      for (f <- allFiles(file)) {
        f.delete()
      }
    }
  }
}
