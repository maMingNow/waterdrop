package io.github.interestinglab.waterdrop.apis

import com.typesafe.config.Config
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

//����Config�������ļ������ĳһ��������õ����Լ���
abstract class BaseInput(initConfig: Config) extends Plugin {

  /**
   * No matter what kind of Input it is, all you have to do is create a DStream to be used latter
   * ����ʱ�䴰���ڵ�DStream��
   * */
  def getDStream(ssc: StreamingContext): DStream[(String, String)]

  /**
   * Things to do after filter and before output
   * ��������Output֮ǰ��������
   * */
  def beforeOutput: Unit = {}

  /**
   * Things to do after output, such as update offset
   * ��������Output֮���������--�������kafka��offset
   * */
  def afterOutput: Unit = {}

}
