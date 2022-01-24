package eu.szakallas

import org.scalatest._
import flatspec._
import matchers._
import org.apache.spark.sql.SparkSession
import org.apache.spark.util.SerializableConfiguration
import Inspectors._

class SparkExampleSpec extends AnyFlatSpec with should.Matchers {
  lazy val spark = SparkSession.builder
    .appName("SparkMatrixExample")
    .master("local[*]")
    .getOrCreate()

  "SparkExample" should s"work on Spark ${spark.version} / Scala ${util.Properties.versionNumberString}" in {
    val results = SerializableConfigExample.run(spark)

    forAll (results) { _ shouldBe true }
  }
}

