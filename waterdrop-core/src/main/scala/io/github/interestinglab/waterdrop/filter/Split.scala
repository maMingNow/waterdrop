package io.github.interestinglab.waterdrop.filter

import com.typesafe.config.{Config, ConfigFactory}
import io.github.interestinglab.waterdrop.apis.BaseFilter
import io.github.interestinglab.waterdrop.core.RowConstant
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.StreamingContext

import scala.collection.JavaConversions._

//�����ݽ��в��
class Split(var conf: Config) extends BaseFilter(conf) {

  def this() = {
    this(ConfigFactory.empty())
  }

  // TODO: check fields.length == field_types.length if field_types exists
  //�����в�ֺ����Щ�м���
  override def checkConfig(): (Boolean, String) = {
    conf.hasPath("fields") && conf.getStringList("fields").size() > 0 match {
      case true => (true, "")
      case false => (false, "please specify [fields] as a non-empty string list")
    }
  }

  override def prepare(spark: SparkSession, ssc: StreamingContext): Unit = {

    //Ĭ������ֵ
    val defaultConfig = ConfigFactory.parseMap(
      Map(
        "delimiter" -> " ",//����ʲô���Ž��в��
        "source_field" -> "raw_message",//���ĸ��ֶν��в��
        "target_field" -> RowConstant.ROOT
      )
    )

    conf = conf.withFallback(defaultConfig)
  }

  override def process(spark: SparkSession, df: DataFrame): DataFrame = {

    val srcField = conf.getString("source_field") //���ĸ��ֶν��в��
    val keys = conf.getStringList("fields") //��ֺ���м���

    // https://stackoverflow.com/a/33345698/1145750
    conf.getString("target_field") match {
      case RowConstant.ROOT => {
        val func = udf((s: String) => {//�Զ���udf�������ж��ַ������.����ֵ�ǲ�ֺ�ļ���
          split(s, conf.getString("delimiter"), keys.size())
        })
        var filterDf = df.withColumn(RowConstant.TMP, func(col(srcField)))
        for (i <- 0 until keys.size()) {
          filterDf = filterDf.withColumn(keys.get(i), col(RowConstant.TMP)(i)) //׷��ÿһ����Ҫ����
        }
        filterDf.drop(RowConstant.TMP)
      }
      case targetField: String => {
        //�Զ���һ��udf����,���ַ����������в����
        val func = udf((s: String) => {
          val values = split(s, conf.getString("delimiter"), keys.size)
          val kvs = (keys zip values).toMap
          kvs
        })

        df.withColumn(targetField, func(col(srcField))) //Ŀ������һ��map����,����ֵ�����key�����յ�valueֵ֮���ӳ�������ַ�����ʽ
      }
    }
  }

  /**
   * Split string by delimiter, if size of splited parts is less than fillLength,
   * empty string is filled; if greater than fillLength, parts will be truncated.
   * ���ز�ֺ�ļ���
   * */
  private def split(str: String, delimiter: String, fillLength: Int): Seq[String] = {
    val parts = str.split(delimiter).map(_.trim) //��ֳ����ɶ�
    val filled = (fillLength compare parts.size) match {
      case 0 => parts
      case 1 => parts ++ Array.fill[String](fillLength - parts.size)("")
      case -1 => parts.slice(0, fillLength)
    }
    filled.toSeq
  }
}
