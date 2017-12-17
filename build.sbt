name         := "Waterdrop"
version      := "0.1.0"
organization := "org.interestinglab.waterdrop"

scalaVersion := "2.11.8"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}