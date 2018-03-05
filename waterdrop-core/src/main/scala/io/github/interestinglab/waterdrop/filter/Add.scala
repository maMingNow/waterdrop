package io.github.interestinglab.waterdrop.filter

import com.typesafe.config.{Config, ConfigFactory}
import io.github.interestinglab.waterdrop.apis.BaseFilter
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.lit

//追加一个列以及对应的值
class Add(var conf: Config) extends BaseFilter(conf) {

  def this() = {
    this(ConfigFactory.empty())
  }

  override def checkConfig(): (Boolean, String) = {
    //必须有追加的列名 以及 追加的列的值
    conf.hasPath("target_field") && conf.hasPath("value") match {
      case true => (true, "")
      case false => (false, "please specify [target_field], [value]")
    }
  }
  
  //追加列名字以及列值
  override def process(spark: SparkSession, df: DataFrame): DataFrame = {
    df.withColumn(conf.getString("target_field"), lit(conf.getString("value")))
  }
}
