name := "foundation-db-scala"

organization := "me.archdev"

scalaVersion := "2.12.7"

crossScalaVersions := Seq("2.12.7", "2.11.12")

libraryDependencies += "com.apple" % "foundationdb" % "6.0.15" from "https://www.foundationdb.org/downloads/6.0.15/bindings/java/fdb-java-6.0.15.jar"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.typelevel" %% "cats-effect" % "1.0.0",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
  "com.chuusai" %% "shapeless" % "2.3.3"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.dimafeng" %% "testcontainers-scala" % "0.21.0" % "test"
)

scalacOptions += "-Ypartial-unification"
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

parallelExecution in ThisBuild := false

inThisBuild(List(
  organization := "me.archdev",
  homepage := Some(url("https://github.com/archdev/foundationdb-scala-client")),
  licenses := List("MIT" -> url("https://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      "arhelmus",
      "Arthur Kushka",
      "arhelmus@gmail.com",
      url("https://archdev.me")
    )
  )
))