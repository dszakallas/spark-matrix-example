import sbt._
import SparkVersionAxis._

ThisBuild / scalaVersion := "2.13.6"

val commonSettings = Seq(
  version := "0.0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  ),
  assembly / assemblyJarName := {
    { moduleName.value + "_" + scalaBinaryVersion.value + "-" + version.value + ".assembly.jar" }
  },
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

val Spark2DynamoDB = "com.audienceproject" %% "spark-dynamodb" % "1.0.4"

val Spark3DynamoDB = "com.audienceproject" %% "spark-dynamodb" % "1.1.2" excludeAll (
  ExclusionRule("com.fasterxml.jackson.core")
)

val `spark-matrix-example` = (projectMatrix in file("spark-matrix-example"))
  .settings(commonSettings: _*)
  .sparkRow(SparkVersionAxis("2.4.7"),
            scalaVersions = Seq("2.11.12"),
            settings = Seq(
              libraryDependencies ++= Seq(Spark2DynamoDB)
            )
  )
  .sparkRow(SparkVersionAxis("3.1.2"),
            scalaVersions = Seq("2.12.12"),
            settings = Seq(
              libraryDependencies ++= Seq(Spark3DynamoDB)
            )
  )
  .sparkRow(SparkVersionAxis("3.2.0"),
            scalaVersions = Seq("2.12.12", "2.13.6"),
            settings = Seq(
              libraryDependencies ++= { if (!isScala2_13(virtualAxes.value)) Seq(Spark3DynamoDB) else Seq() }
            )
  )
