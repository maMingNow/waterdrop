package io.github.interestinglab.waterdrop

import java.io.File

import io.github.interestinglab.waterdrop.apis.{BaseFilter, BaseInput, BaseOutput}
import io.github.interestinglab.waterdrop.config.{CommandLineArgs, CommandLineUtils, Common, ConfigBuilder}
import io.github.interestinglab.waterdrop.filter.UdfRegister
import io.github.interestinglab.waterdrop.utils.CompressionUtils
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.streaming._

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

object Waterdrop extends Logging {

  def main(args: Array[String]) {

    CommandLineUtils.parser.parse(args, CommandLineArgs()) match {
      case Some(cmdArgs) => { //说明可以解析参数成功
        Common.setDeployMode(cmdArgs.deployMode) //设置运行模式

	//设置配置文件,可能是集群上的路径,或者是本地路径
        val configFilePath = Common.getDeployMode match {
          case Some(m) => {
            if (m.equals("cluster")) {
              // only keep filename in cluster mode
              new Path(cmdArgs.configFile).getName
            } else {
              cmdArgs.configFile
            }
          }
        }

        cmdArgs.testConfig match {
          case true => {
            new ConfigBuilder(cmdArgs.configFile)
            println("config OK !")
            // TODO: check config
          }
          case false => {
            entrypoint(cmdArgs.configFile)
          }
        }
      }
      case None =>
      // CommandLineUtils.parser.showUsageAsError()
      // CommandLineUtils.parser.terminate(Right(()))
    }
  }

  private def entrypoint(configFile: String): Unit = {

    val configBuilder = new ConfigBuilder(configFile)
    val sparkConfig = configBuilder.getSparkConfigs //获取spark模块对应的配置信息
    //解析配置的实例化插件对象
    val inputs = configBuilder.createInputs
    val outputs = configBuilder.createOutputs
    val filters = configBuilder.createFilters

    //对插件的配置进行校验
    var configValid = true //默认全部插件解析成功
    val plugins = inputs ::: filters ::: outputs //收集所有的插件集合
    for (p <- plugins) {//循环所有的插件
      val (isValid, msg) = Try(p.checkConfig) match {
        case Success(info) => {
          val (ret, message) = info
          (ret, message)
        }
        case Failure(exception) => (false, exception.getMessage) //出现异常,异常的原因
      }

      if (!isValid) {//说明遇见解析失败的了
        configValid = false
        printf("Plugin[%s] contains invalid config, error: %s\n", p.name, msg) //打印说明哪个插件解析有问题,问题原因是什么
      }
    }

    if (!configValid) {//解析有问题,则退出程序
      System.exit(-1) // invalid configuration
    }
  
    //以下内容表示解析插件成功
    println("[INFO] loading SparkConf: ")
    val sparkConf = createSparkConf(configBuilder)//根据配置文件生成spark的配置文件
    //打印spark的配置文件
    sparkConf.getAll.foreach(entry => {
      val (key, value) = entry
      println("\t" + key + " => " + value)
    })

    val duration = sparkConfig.getLong("spark.streaming.batchDuration")
    val ssc = new StreamingContext(sparkConf, Seconds(duration))
    val sparkSession = SparkSession.builder.config(ssc.sparkContext.getConf).getOrCreate()

    Common.getDeployMode match {
      case Some(m) => {
        if (m.equals("cluster")) {

          logInfo("preparing cluster mode work dir files...")

          // plugins.tar.gz is added in local app temp dir of driver and executors in cluster mode from --files specified in spark-submit
          val workDir = new File(".")
          logWarning("work dir exists: " + workDir.exists() + ", is dir: " + workDir.isDirectory)

          workDir.listFiles().foreach(f => logWarning("\t list file: " + f.getAbsolutePath))

          // decompress plugin dir
          val compressedFile = new File("plugins.tar.gz")

          Try(CompressionUtils.unGzip(compressedFile, workDir)) match {//在当前文件夹下解析插件
            case Success(tempFile) => {
              Try(CompressionUtils.unTar(tempFile, workDir)) match {
                case Success(_) => logInfo("succeeded to decompress plugins.tar.gz")
                case Failure(ex) => {
                  logError("failed to decompress plugins.tar.gz", ex)
                  sys.exit(-1)
                }
              }

            }
            case Failure(ex) => {
              logError("failed to decompress plugins.tar.gz", ex)
              sys.exit(-1)
            }
          }
        }
      }
    }

    process(sparkSession, ssc, inputs, filters, outputs)
  }

  //真正的处理开启sparkStreaming程序
  //整体流程----1.每个周期加载所有input输入流----2.进行filter处理---3.inputs.beforeOutput
  //---4.outputs.process(df)---5.inputs.afterOutput
  //总结 input---filter--outputs。只是在outputs的前后,input有一个拦截器进行拦截
  private def process(
    sparkSession: SparkSession,
    ssc: StreamingContext,
    inputs: List[BaseInput],
    filters: List[BaseFilter],
    outputs: List[BaseOutput]): Unit = {

    // find all user defined UDFs and register in application init
    UdfRegister.findAndRegisterUdfs(sparkSession)

    //每一个插件设置一些准备工作
    for (i <- inputs) {
      i.prepare(sparkSession, ssc)
    }

    for (o <- outputs) {
      o.prepare(sparkSession, ssc)
    }

    for (f <- filters) {
      f.prepare(sparkSession, ssc)
    }

    //以下内容将会产生RDD,在多个节点运行
    //每一个input对应一个DStream流---流集合
    val dstreamList = inputs.map(p => {
      p.getDStream(ssc)
    })

    //汇总所有的流集合
    val unionedDStream = dstreamList.reduce((d1, d2) => {
      d1.union(d2)
    })

    val dStream = unionedDStream.mapPartitions { partitions =>
      val strIterator = partitions.map(r => r._2)
      val strList = strIterator.toList
      strList.iterator
    }

    dStream.foreachRDD { strRDD =>
      val rowsRDD = strRDD.mapPartitions { partitions =>
        val row = partitions.map(Row(_)) //每一行具体的内容变成Row对象
        val rows = row.toList
        rows.iterator
      }

      val spark = SparkSession.builder.config(rowsRDD.sparkContext.getConf).getOrCreate()

      val schema = StructType(Array(StructField("raw_message", StringType))) //表示每一行具体的内容
      var df = spark.createDataFrame(rowsRDD, schema)

      //过滤器对每一行具体的内容进行过滤,产生新的DataFrame对象
      for (f <- filters) {
        df = f.process(spark, df) //即每一个filter上的df参数的数据格式都是不同的,因此filter的顺序很重要
      }

      //filter之后,output之前,每一个input插件可以做一些事情
      inputs.foreach(p => {
        p.beforeOutput
      })

      //输出插件对最终结果进行输出
      outputs.foreach(p => {
        p.process(df)
      })

      //输出之后,input可以做什么
      inputs.foreach(p => {
        p.afterOutput
      })

    }

    ssc.start()
    ssc.awaitTermination()
  }

  //创建spark的配置文件
  private def createSparkConf(configBuilder: ConfigBuilder): SparkConf = {
    val sparkConf = new SparkConf()

    configBuilder.getSparkConfigs //使用配置文件中的配置项覆盖掉默认内容
      .entrySet()
      .foreach(entry => {
        sparkConf.set(entry.getKey, String.valueOf(entry.getValue.unwrapped()))
      })

    sparkConf
  }
}
