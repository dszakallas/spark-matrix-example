package eu.szakallas

import org.apache.spark.sql.SparkSession
import org.apache.spark.util.SerializableConfiguration

object SerializableConfigExample {

  def run(spark: SparkSession): Array[Boolean] = {
    val hadoopConf = spark.sparkContext.hadoopConfiguration
    hadoopConf.set("mykey", "myvalue")
    val serializableConf = new SerializableConfiguration(hadoopConf)

    spark.sparkContext
      .parallelize(1 until 10)
      .map { _ => serializableConf.value.get("mykey") == "myvalue" }
      .collect()
  }

}
