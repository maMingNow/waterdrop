package io.github.interestinglab.waterdrop.config

case class CommandLineArgs(
  deployMode: String = "client",//spark的启动模式
  configFile: String = "application.conf",//配置文件路径
  testConfig: Boolean = false) //是否测试
