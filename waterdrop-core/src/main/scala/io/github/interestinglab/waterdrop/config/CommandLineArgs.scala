package io.github.interestinglab.waterdrop.config

case class CommandLineArgs(
  deployMode: String = "client",//spark������ģʽ
  configFile: String = "application.conf",//�����ļ�·��
  testConfig: Boolean = false) //�Ƿ����
