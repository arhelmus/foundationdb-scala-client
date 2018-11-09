FoundationDB Scala client
=========================
[![Build Status](https://travis-ci.com/ArchDev/foundationdb-scala-client.svg?branch=master)](https://travis-ci.com/ArchDev/foundationdb-scala-client)

This is a functional Scala wrapper for Java FoundationDB driver.
Goal of it is to provide easy to use access to FoundationDB from Scala.
**If you are using FoundationDB in your Scala project already, please [contact me](https://archdev.me).**   

Work in progress:
- [x] Transaction DSL concept
- [x] Generic serialization
- [x] Namespaces control
- [x] Optimization of async effects
- [x] Public CI setup and release
- [x] Directory management
- [ ] Integration and Unit tests
- [ ] Full support of transaction DSL 

## Example
Current example is a design concept, if you have ideas how to make it better, feel free to open an Issue or PR.
```scala
  import me.archdev.foundationdb._
  import me.archdev.foundationdb.namespaces._

  val fdb = FoundationDB.connect(520, "path/to/fdb.cluster")

  implicit val subspace: Subspace =
    fdb.openDirectorySync(Seq("my", "directory", "path")).buildSubspace("test_subspace")
    
  import fdb.syntax._
  fdb.execute(
      for {
        _ <- set("key", SomeStorageModel("1", 2, "3"))
        _ <- set("key2", "value2")
        result <- get[String, SomeStorageModel]("key")
        _ <- delete("key2")
      } yield result
  )
```

There is no release yet, but you can use snapshot to try it:
```scala
libraryDependencies += "me.archdev" %% "foundation-db-scala" % "0.0.1-SNAPSHOT"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
```

## Copyright
Copyright (C) 2018 Arthur Kushka.  
Distributed under the MIT License.

## Contact
Wanna ask me something or stay in touch?   
Follow my Twitter [@arhelmus](https://twitter.com/Arhelmus)
