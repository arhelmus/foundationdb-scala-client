name := "foundation-db-scala"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "org.foundationdb" % "fdb-java" % "5.2.5"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.0.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"

scalacOptions += "-Ypartial-unification"
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)