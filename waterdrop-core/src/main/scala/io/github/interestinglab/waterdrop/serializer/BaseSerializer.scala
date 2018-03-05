package io.github.interestinglab.waterdrop.serializer

import com.typesafe.config.Config


// TODO : 是否需要checkConfig, prepare, 何时调用serializer的serialize, deserialize
abstract class BaseSerializer(config: Config) {

  val charset = if (config.hasPath("charset")) {//序列化的编码方式
    config.getString("charset")
  } else {
    "utf-8"
  }

  /**
   * Deserialize array of bytes to String.
   * 反序列化
   */
  def deserialize(bytes: Array[Byte]): String

  /**
   * Serialize String to bytes of array.序列化
   */
  def serialize(e: String): Array[Byte]
}
