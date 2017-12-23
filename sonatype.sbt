// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "io.github.interestinglab"

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// License of your choice
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://interestinglab.github.io/waterdrop/"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/Interestinglab/waterdrop"),
    "scm:git@github.com:Interestinglab/waterdrop.git"
  )
)
developers := List(
  Developer(id="interestinglab", name="interestinglab", email="huochen1994@163.com", url=url("https://github.com/Interestinglab"))
)