package io.github.interestinglab.waterdrop.apis

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext

/**
 * checkConfig --> prepare
 * 表示定义一个插件
 */
abstract class Plugin extends Serializable with Logging {

  /**
   *  Return true and empty string if config is valid, return false and error message if config is invalid.
   * 对插件的配置进行校验,返回校验是否成功,以及失败的时候打印校验失败原因
   */
  def checkConfig(): (Boolean, String)

  /**
   * Get Plugin Name.
   * 插件的name
   */
  def name: String = this.getClass.getName

  /**
   * Prepare before running, do things like set config default value, add broadcast variable, accumulator.
   * 运行之前,做一些准备工作,在driver上执行的
   * 比如配置默认值、添加广播变量等
   */
  def prepare(spark: SparkSession, ssc: StreamingContext): Unit = {}
}
