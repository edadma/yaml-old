name := "yaml"

version := "0.2"

scalaVersion := "2.13.0"

scalacOptions ++= Seq( "-deprecation", "-feature", "-unchecked", "-language:postfixOps", "-language:implicitConversions", "-language:existentials" )

organization := "xyz.hyperreal"

//resolvers += Resolver.sonatypeRepo( "snapshots" )

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "3.0.8" % "test",
	"org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
)

libraryDependencies ++= Seq(
	"org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
//	"org.scala-lang.modules" %% "scala-xml" % "1.0.6"
//	"org.scala-lang.modules" %% "scala-swing" % "2.0.3"
)

//libraryDependencies ++= Seq(
//  "com.typesafe" % "config" % "1.3.3"
//)

//libraryDependencies ++= Seq(
//  "jline" % "jline" % "2.14.6"
//)

libraryDependencies ++= Seq(
  "xyz.hyperreal" %% "indentation-lexical" % "0.9"
)

coverageExcludedPackages := ".*Main;.*package"

mainClass in (Compile, run) := Some( "xyz.hyperreal." + name.value.replace('-', '_') + ".Main" )

mainClass in assembly := Some( "xyz.hyperreal." + name.value.replace('-', '_') + ".Main" )

assemblyJarName in assembly := name.value + "-" + version.value + ".jar"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/edadma/" + name.value))

pomExtra :=
  <scm>
    <url>git@github.com:edadma/{name.value}.git</url>
    <connection>scm:git:git@github.com:edadma/{name.value}.git</connection>
  </scm>
  <developers>
    <developer>
      <id>edadma</id>
      <name>Edward A. Maxedon, Sr.</name>
      <url>https://github.com/edadma</url>
    </developer>
  </developers>
