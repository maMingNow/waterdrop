package io.github.interestinglab.waterdrop.apis

import com.typesafe.config.Config
import org.apache.spark.sql.DataFrame

abstract class BaseOutput(initConfig: Config) extends Plugin {
  //��ζԽ����DataFrame�������,�������������̨��hdfs��mysql��
  def process(df: DataFrame)
}
