package io.github.interestinglab.waterdrop.apis

import com.typesafe.config.Config
import org.apache.spark.sql.expressions.{UserDefinedAggregateFunction, UserDefinedFunction}
import org.apache.spark.sql.{DataFrame, SparkSession}

abstract class BaseFilter(val initConfig: Config) extends Plugin {

  //如何对一个DataFrame进行处理,产生新的DataFrame,新的DataFrame和老的DataFrame可以字段不一致,因此filter的顺序很重要
  def process(spark: SparkSession, df: DataFrame): DataFrame

  /**
   * Allow to register user defined UDFs
   * @return empty list if there is no UDFs to be registered
   * */
  def getUdfList(): List[(String, UserDefinedFunction)] = List.empty

  /**
   * Allow to register user defined UDAFs
   * @return empty list if there is no UDAFs to be registered
   * */
  def getUdafList(): List[(String, UserDefinedAggregateFunction)] = List.empty
}
