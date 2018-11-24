val fetchedCredentials = for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Seq(Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  ))

publishTo := sonatypePublishTo.value

sonatypeProfileName := "arhelmus"

publishMavenStyle := true

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("archdev", "foundationdb-scala-client", "arhelmus@gmail.com"))

// or if you want to set these fields manually
homepage := Some(url("https://github.com/archdev/foundationdb-scala-client"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/archdev/foundationdb-scala-client"),
    "scm:git@github.com:ArchDev/foundationdb-scala-client.git"
  )
)

developers := List(
  Developer(id="arhelmus", name="Arthur Kushka", email="arhelmus@gmail.com", url=url("https://archdev.me"))
)