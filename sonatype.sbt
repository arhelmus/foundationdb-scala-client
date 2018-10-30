val fetchedCredentials = for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Seq(Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  ))

credentials ++= fetchedCredentials.getOrElse(Nil)

publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/ArchDev/foundationdb-scala-client</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>https://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:ArchDev/foundationdb-scala-client.git</url>
      <connection>scm:git:git@github.com:ArchDev/foundationdb-scala-client.git</connection>
    </scm>
    <developers>
      <developer>
        <id>ArchDev</id>
        <name>Arthur Kushka</name>
        <url>https://www.archdev.me</url>
      </developer>
    </developers>)