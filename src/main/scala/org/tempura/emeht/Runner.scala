package org.tempura.emeht

import org.sellmerfud.optparse.OptionParser

import scala.util.{Try, Success, Failure}

case class RunnerOpts(sourceSettings: java.io.File = null,
                      settingsType: String = "intellij",
                      cleanUpAfter: Boolean = true)

// sbt 'run-main org.tempura.emeht --no_cleanup --settings_type intellij settings.jar'
object Runner extends App { self =>
  def getOpts: Try[RunnerOpts] = {
    Try {
      val opts = new OptionParser[RunnerOpts] {
        banner = "%s [--no_cleanup] path/to/a/settings.jar".format(self.getClass.getName)
        separator("")
        separator("Options:")

        flag(
          short = "-c",
          long  = "--no_cleanup",
          info  = "If set, when applicable will remove any inflated temporary files; default is false")(cfg => cfg.copy(cleanUpAfter = false))

        reqd[String](
          short = "-t",
          long  = "--settings_type",
          info  = "Type of settings file to invert colors for; currently 'intellij' and 'sublime'/'textmate' supported; IntelliJ is default."
        )((p, cfg) => cfg.copy(settingsType = p.toLowerCase))

        arg[java.io.File]((p, cfg) => cfg.copy(sourceSettings = p))
      }.parse(args, RunnerOpts())
      if (opts.sourceSettings == null) {
        throw new Exception("Path to a settings file is required as an argument!")
      }
      opts
    }
  }

  getOpts match {
    case Success(opts) => new EmehtRunner(opts).run()
    case Failure(error) => println("Error occurred: " + error)
  }
}

class EmehtRunner(opts: RunnerOpts) {
  def run() = opts.settingsType match {
    case "intellij" =>
      IntellijColorSettingsInverter.invertColorsForSettingsJar(opts.sourceSettings, opts.cleanUpAfter)
    case "textmate" | "sublime" =>
      TextMateThemeInverter.invertColorsForTmTheme(opts.sourceSettings)
    case t => println(s"Unsupported settings file type $t; doing nothing.")
  }
}