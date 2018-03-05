package io.github.interestinglab.waterdrop.apis

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext

/**
 * checkConfig --> prepare
 * ��ʾ����һ�����
 */
abstract class Plugin extends Serializable with Logging {

  /**
   *  Return true and empty string if config is valid, return false and error message if config is invalid.
   * �Բ�������ý���У��,����У���Ƿ�ɹ�,�Լ�ʧ�ܵ�ʱ���ӡУ��ʧ��ԭ��
   */
  def checkConfig(): (Boolean, String)

  /**
   * Get Plugin Name.
   * �����name
   */
  def name: String = this.getClass.getName

  /**
   * Prepare before running, do things like set config default value, add broadcast variable, accumulator.
   * ����֮ǰ,��һЩ׼������,��driver��ִ�е�
   * ��������Ĭ��ֵ����ӹ㲥������
   */
  def prepare(spark: SparkSession, ssc: StreamingContext): Unit = {}
}
