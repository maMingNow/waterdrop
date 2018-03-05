package io.github.interestinglab.waterdrop.apis

import com.typesafe.config.Config
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

//参数Config是配置文件中针对某一个插件设置的属性集合
abstract class BaseInput(initConfig: Config) extends Plugin {

  /**
   * No matter what kind of Input it is, all you have to do is create a DStream to be used latter
   * 产生时间窗口内的DStream流
   * */
  def getDStream(ssc: StreamingContext): DStream[(String, String)]

  /**
   * Things to do after filter and before output
   * 拦截器对Output之前进行拦截
   * */
  def beforeOutput: Unit = {}

  /**
   * Things to do after output, such as update offset
   * 拦截器对Output之后进行拦截--比如更新kafka的offset
   * */
  def afterOutput: Unit = {}

}
