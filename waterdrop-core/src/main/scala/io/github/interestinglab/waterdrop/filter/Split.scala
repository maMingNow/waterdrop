package io.github.interestinglab.waterdrop.filter

import com.typesafe.config.{Config, ConfigFactory}
import io.github.interestinglab.waterdrop.apis.BaseFilter
import io.github.interestinglab.waterdrop.core.RowConstant
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.StreamingContext

import scala.collection.JavaConversions._

//对数据进行拆分
class Split(var conf: Config) extends BaseFilter(conf) {

  def this() = {
    this(ConfigFactory.empty())
  }

  // TODO: check fields.length == field_types.length if field_types exists
  //必须有拆分后的哪些列集合
  override def checkConfig(): (Boolean, String) = {
    conf.hasPath("fields") && conf.getStringList("fields").size() > 0 match {
      case true => (true, "")
      case false => (false, "please specify [fields] as a non-empty string list")
    }
  }

  override def prepare(spark: SparkSession, ssc: StreamingContext): Unit = {

    //默认配置值
    val defaultConfig = ConfigFactory.parseMap(
      Map(
        "delimiter" -> " ",//按照什么符号进行拆分
        "source_field" -> "raw_message",//对哪个字段进行拆分
        "target_field" -> RowConstant.ROOT
      )
    )

    conf = conf.withFallback(defaultConfig)
  }

  override def process(spark: SparkSession, df: DataFrame): DataFrame = {

    val srcField = conf.getString("source_field") //对哪个字段进行拆分
    val keys = conf.getStringList("fields") //拆分后的列集合

    // https://stackoverflow.com/a/33345698/1145750
    conf.getString("target_field") match {
      case RowConstant.ROOT => {
        val func = udf((s: String) => {//自定义udf函数进行对字符串拆分.返回值是拆分后的集合
          split(s, conf.getString("delimiter"), keys.size())
        })
        var filterDf = df.withColumn(RowConstant.TMP, func(col(srcField)))
        for (i <- 0 until keys.size()) {
          filterDf = filterDf.withColumn(keys.get(i), col(RowConstant.TMP)(i)) //追加每一个需要的列
        }
        filterDf.drop(RowConstant.TMP)
      }
      case targetField: String => {
        //自定义一个udf函数,对字符串参数进行拆分呢
        val func = udf((s: String) => {
          val values = split(s, conf.getString("delimiter"), keys.size)
          val kvs = (keys zip values).toMap
          kvs
        })

        df.withColumn(targetField, func(col(srcField))) //目标列是一个map对象,即拆分的属性key与最终的value值之间的映射对象的字符串形式
      }
    }
  }

  /**
   * Split string by delimiter, if size of splited parts is less than fillLength,
   * empty string is filled; if greater than fillLength, parts will be truncated.
   * 返回拆分后的集合
   * */
  private def split(str: String, delimiter: String, fillLength: Int): Seq[String] = {
    val parts = str.split(delimiter).map(_.trim) //拆分成若干段
    val filled = (fillLength compare parts.size) match {
      case 0 => parts
      case 1 => parts ++ Array.fill[String](fillLength - parts.size)("")
      case -1 => parts.slice(0, fillLength)
    }
    filled.toSeq
  }
}
