package io.github.interestinglab.waterdrop.input

import com.typesafe.config.Config
import io.github.interestinglab.waterdrop.apis.BaseInput
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

//每隔一定周期读取hdfs上的一个文件
class Hdfs(config: Config) extends BaseInput(config) {

  override def checkConfig(): (Boolean, String) = {
    config.hasPath("path") match {//必须存在path路径
      case true => {

        val dir = config.getString("path")
        val path = new org.apache.hadoop.fs.Path(dir)
        Option(path.toUri.getScheme) match {//必须有schema或者没有设置schema
          case None => (true, "")
          case Some(schema) => (true, "")
          case _ => //说明是不支持的schema
            (
              false,
              "unsupported schema, please set the following allowed schemas: hdfs://, for example: hdfs://<name-service>:<port>/var/log")
        }
      }
      case false => (false, "please specify [path] as non-empty string")
    }
  }

  override def getDStream(ssc: StreamingContext): DStream[(String, String)] = {

    ssc.textFileStream(config.getString("path")).map(s => { ("", s) })//s表示文件内容
  }
}
