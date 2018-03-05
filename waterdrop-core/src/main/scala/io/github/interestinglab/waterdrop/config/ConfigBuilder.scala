package io.github.interestinglab.waterdrop.config

import scala.language.reflectiveCalls
import scala.collection.JavaConversions._
import com.typesafe.config.{Config, ConfigRenderOptions}
import io.github.interestinglab.waterdrop.apis.{BaseFilter, BaseInput, BaseOutput}
import org.antlr.v4.runtime.{ANTLRFileStream, CharStream, CommonTokenStream}
import io.github.interestinglab.waterdrop.configparser.{ConfigLexer, ConfigParser, ConfigVisitor}

//参数带有配置文件路径
class ConfigBuilder(configFile: String) {

  val config = load()

  def load(): Config = {

    // val configFile = System.getProperty("config.path", "")
    if (configFile == "") {
      throw new ConfigRuntimeException("Please specify config file")
    }

    println("[INFO] Loading config file: " + configFile)

    // CharStreams is for Antlr4.7
    // val charStream: CharStream = CharStreams.fromFileName(configFile)
    val charStream: CharStream = new ANTLRFileStream(configFile) //加载配置文件
    val lexer: ConfigLexer = new ConfigLexer(charStream)
    val tokens: CommonTokenStream = new CommonTokenStream(lexer)
    val parser: ConfigParser = new ConfigParser(tokens)

    val configContext: ConfigParser.ConfigContext = parser.config
    val visitor: ConfigVisitor[Config] = new ConfigVisitorImpl

    val parsedConfig = visitor.visit(configContext)

    val options: ConfigRenderOptions = ConfigRenderOptions.concise.setFormatted(true)
    System.out.println("[INFO] Parsed Config: \n" + parsedConfig.root().render(options))

    parsedConfig
  }

  //获取spark模块对应的配置信息
  def getSparkConfigs: Config = {
    config.getConfig("spark")
  }

  def createFilters: List[BaseFilter] = {

    var filterList = List[BaseFilter]()
    config
      .getConfigList("filter")
      .foreach(plugin => {
        val className = buildClassFullQualifier(plugin.getString(ConfigBuilder.PluginNameKey), "filter")

        val obj = Class
          .forName(className)
          .getConstructor(classOf[Config])
          .newInstance(plugin.getConfig(ConfigBuilder.PluginParamsKey))
          .asInstanceOf[BaseFilter]

        filterList = filterList :+ obj
      })

    filterList
  }

  //创建实例化对象
  def createInputs: List[BaseInput] = {

    var inputList = List[BaseInput]()
    config
      .getConfigList("input") //获取input模块配置内容
      .foreach(plugin => {
        //plugin.getString(ConfigBuilder.PluginNameKey)表示获取插件的name属性,
	//获取class的全路径
        val className = buildClassFullQualifier(plugin.getString(ConfigBuilder.PluginNameKey), "input")
	//创建实例化对象
        val obj = Class
          .forName(className)
          .getConstructor(classOf[Config])
          .newInstance(plugin.getConfig(ConfigBuilder.PluginParamsKey)) //该插件配置的参数集合
          .asInstanceOf[BaseInput]

        inputList = inputList :+ obj
      })

    inputList
  }

  def createOutputs: List[BaseOutput] = {

    var outputList = List[BaseOutput]()
    config
      .getConfigList("output")
      .foreach(plugin => {
        val className = buildClassFullQualifier(plugin.getString(ConfigBuilder.PluginNameKey), "output")

        val obj = Class
          .forName(className)
          .getConstructor(classOf[Config])
          .newInstance(plugin.getConfig(ConfigBuilder.PluginParamsKey))
          .asInstanceOf[BaseOutput]

        outputList = outputList :+ obj
      })

    outputList
  }

  //name表示插件的全路径,classType表示插件类型,即是input还是filter等
  //返回插件的全路径
  private def buildClassFullQualifier(name: String, classType: String): String = {
    var qualifier = name;
    if (qualifier.split("\\.").length == 1) {//说明只给了插件的class类名

      val packageName = classType match {
        case "input" => ConfigBuilder.InputPackage
        case "filter" => ConfigBuilder.FilterPackage
        case "output" => ConfigBuilder.OutputPackage
      }

      qualifier = packageName + "." + qualifier.capitalize
    }

    qualifier //说明本身就是全路径
  }
}

object ConfigBuilder {
  //源代码路径
  val PackagePrefix = "io.github.interestinglab.waterdrop"
  val FilterPackage = PackagePrefix + ".filter"
  val InputPackage = PackagePrefix + ".input"
  val OutputPackage = PackagePrefix + ".output"

  //插件key和参数
  val PluginNameKey = "name"
  val PluginParamsKey = "entries"
}
