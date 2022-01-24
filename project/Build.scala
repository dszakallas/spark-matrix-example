import sbt._
import sbt.Keys._
import sbt.VirtualAxis._
import sbt.internal.ProjectMatrix
import sbtprojectmatrix.ProjectMatrixKeys._

case class SparkVersionAxis(sparkVersion: String) extends sbt.VirtualAxis.WeakAxis {
  val sparkVersionCompat: String = sparkVersion.split("\\.", 3).take(2).mkString(".")
  override val directorySuffix = s"-spark${sparkVersionCompat}"
  override val idSuffix: String = directorySuffix.replaceAll("""\W+""", "_")
}

object SparkVersionAxis {
  private def sparkDeps(version: String, modules: Seq[String]) = for {module <- modules} yield {
    "org.apache.spark" %% s"spark-${module}" % version % Provided
  }

  def isScala2_13(axes: Seq[VirtualAxis]) = {
    axes.collectFirst{ case ScalaVersionAxis(_, scalaVersionCompat) => scalaVersionCompat }.map(_ == "2.13").getOrElse(true)
  }

  def isSpark2_4(axes: Seq[VirtualAxis]) = {
    axes.collectFirst{ case v@SparkVersionAxis(_) => v.sparkVersionCompat }.map(_ == "2.4").getOrElse(false)
  }

  private val classVersion = System.getProperty("java.class.version").toFloat

  implicit class ProjectExtension(val p: ProjectMatrix) extends AnyVal {
    def sparkRow(sparkAxis: SparkVersionAxis, scalaVersions: Seq[String], settings: Def.SettingsDefinition*): ProjectMatrix =
      p.customRow(
        scalaVersions = scalaVersions,
        axisValues = Seq(sparkAxis, VirtualAxis.jvm),
        _
          .settings(
            moduleName := name.value + sparkAxis.directorySuffix,
            libraryDependencies ++= sparkDeps(sparkAxis.sparkVersion, Seq("core", "sql", "hive")),

            scalacOptions += {
              if (isScala2_13(virtualAxes.value)) {
                "-target:jvm-11"
              } else {
                "-target:jvm-1.8"
              }
            },
            Test / test := {
              if (!(isScala2_13(virtualAxes.value) && classVersion < 55) &&
                    !(isSpark2_4(virtualAxes.value) && classVersion > 52))
                (Test / test).value
            }
          )
          .settings(settings: _*)
      )
  }
}
