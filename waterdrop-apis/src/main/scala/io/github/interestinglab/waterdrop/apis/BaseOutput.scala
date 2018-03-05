package io.github.interestinglab.waterdrop.apis

import com.typesafe.config.Config
import org.apache.spark.sql.DataFrame

abstract class BaseOutput(initConfig: Config) extends Plugin {
  //如何对结果集DataFrame进行输出,比如输出到控制台、hdfs、mysql等
  def process(df: DataFrame)
}
