package io.github.interestinglab.waterdrop.input

import com.typesafe.config.Config
import io.github.interestinglab.waterdrop.apis.BaseInput
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

//ÿ��һ�����ڶ�ȡhdfs�ϵ�һ���ļ�
class Hdfs(config: Config) extends BaseInput(config) {

  override def checkConfig(): (Boolean, String) = {
    config.hasPath("path") match {//�������path·��
      case true => {

        val dir = config.getString("path")
        val path = new org.apache.hadoop.fs.Path(dir)
        Option(path.toUri.getScheme) match {//������schema����û������schema
          case None => (true, "")
          case Some(schema) => (true, "")
          case _ => //˵���ǲ�֧�ֵ�schema
            (
              false,
              "unsupported schema, please set the following allowed schemas: hdfs://, for example: hdfs://<name-service>:<port>/var/log")
        }
      }
      case false => (false, "please specify [path] as non-empty string")
    }
  }

  override def getDStream(ssc: StreamingContext): DStream[(String, String)] = {

    ssc.textFileStream(config.getString("path")).map(s => { ("", s) })//s��ʾ�ļ�����
  }
}
