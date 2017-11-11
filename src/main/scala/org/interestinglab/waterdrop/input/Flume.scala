package org.interestinglab.waterdrop.input

import com.typesafe.config.Config
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.flume._


class Flume(config: Config) extends BaseInput(config) {

  override def checkConfig(): (Boolean, String) = {

    val requiredOptions = List("host", "port", "sinks_type")

    val nonExistsOptions = requiredOptions.map(optionName => (optionName, config.hasPath(optionName))).filter { p =>
      val (optionName, exists) = p
      !exists
    }

    if (nonExistsOptions.length == 0) {

      val sinksTypeAllowedValues = List("spark", "avroSink")

      if (!config.hasPath("sinks_type") || sinksTypeAllowedValues.contains(config.getString("save_mode"))) {
        (true, "")
      } else {
        (false, "wrong value of [sinks_type], allowed values: " + sinksTypeAllowedValues.mkString(", "))
      }

    } else {
      (false, "please specify " + nonExistsOptions.map("[" + _._1 + "]").mkString(", ") + " as non-empty string")
    }
  }

  override def getDStream(ssc: StreamingContext): DStream[(String, String)] = {

    val hostname = config.getString("hostname")
    val port = config.getInt("port")

    config.getString("sinks_type") match {
      case "spark" => FlumeUtils.createPollingStream(ssc, hostname, port).map(s => ("", s.event.getBody.toString))
      case "avroSink" => FlumeUtils.createStream(ssc, hostname, port).map(s => ("", s.event.getBody.toString))
    }
  }
}
