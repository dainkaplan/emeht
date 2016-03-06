package org.tempura.emeht

object ColorOps {
  def handle6(str: String) =
    for (cc <- str.sliding(2,2).toSeq) yield java.lang.Short.valueOf(cc, 16).toShort
  def handle3(str: String) =
    for (c <- str) yield java.lang.Short.valueOf(c.toString * 2, 16).toShort

  implicit class ToHexStr(int: Int) {
    def toHexStr(caps: Boolean = false) = caps match {
      case true => "%02X".format(int)
      case false => "%02x".format(int)
    }
  }

  val hexDigits = Set() ++ ('0' to '9') ++ ('a' to 'f') ++ ('A' to 'F')

  def notHex(digits: String): Boolean =
    digits.exists(c => !hexDigits.contains(c))

  def hexToRGB(str: String): Seq[Short] = {
    val trimmed = if (str startsWith "#") str.drop(1) else str
    if (trimmed.length > 6 || notHex(trimmed)) List()
    else handle6(("0" * (6 - trimmed.length)) + trimmed)
  }

  def hasCaps(in: String) = in.exists(_.isUpper)

  // If passed-in string starts with "#" output string will too.
  // E.g. "FFFFFF" => "000000"
  // E.g. "#FFFFFF" => "#000000"
  def invert(in: String): String = hexToRGB(in) match {
    case List() => in
    case converted => (if (in.startsWith("#")) "#" else "") + (for {
      s <- hexToRGB(in)
      i = 0xFF - s // Actually invert the code
    } yield i.toHexStr(hasCaps(in))).mkString
  }
}
