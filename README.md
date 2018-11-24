FoundationDB Scala client
=========================
[![Build Status](https://travis-ci.com/ArchDev/foundationdb-scala-client.svg?branch=master)](https://travis-ci.com/ArchDev/foundationdb-scala-client)

This is a functional Scala wrapper for Java FoundationDB driver.
Goal of it is to provide easy to use access to FoundationDB from Scala.
**If you are using FoundationDB in your Scala project already, please [contact me](https://archdev.me).**   

## Features:

#### Functional DSL
Main part of the wrapper its an algebra which represents the DSL to manipulate with transactions. 
```scala
val fdb = FoundationDB.connect(600, "path/to/fdb.cluster")
import fdb.syntax._

fdb.execute(
  for {
    _ <- set("key", 1)
    _ <- set("key", 2)
    result <- get[String, Int]("key")
    _ <- clear("key")
  } yield result
)
```   

#### Macros based serialization
We abstracted you over the Tuples and byte arrays that used in java driver.
Our macros will generate correct representation of your data in FoundationDB Tuples.
```scala 
case class SomeStorageModel(a: String, b: Int, c: String)
set("key", SomeStorageModel("1", 2, "3"))
```

#### Implicit subspace control
Instead of manually wrapping all the keys into subspaces, just implicitly provide the subspace to the library and it will do it for you.
```scala
implicit val subspace = Subspace("my-great-subspace")
// Now this query executed in defined subspace
fdb.execute(
  for {
    _ <- set("key", 1)
    result <- get[String, Int]("key")
  } yield result
)
```

#### InMemory testkit
We supporting inmemory runner for the DSL which emulates FoundationDB and tested with the same test spec as real database interpreter.

To use it, just inject different implementation of FoundationDB class in your parameters. 
```scala
FoundationDB.inMemory()
```

## Full example
```scala
  import me.archdev.foundationdb._
  import me.archdev.foundationdb.namespaces._

  val fdb = FoundationDB.connect(600, "path/to/fdb.cluster")

  implicit val subspace: Subspace =
    fdb.openDirectorySync(Seq("my", "directory", "path")).buildSubspace("test_subspace")
    
  import fdb.syntax._
  fdb.execute(
      for {
        _ <- set("key", SomeStorageModel("1", 2, "3"))
        _ <- set("key2", "value2")
        result <- get[String, SomeStorageModel]("key")
        _ <- clear("key2")
      } yield result
  )
```

## Give it a try
We are based on 6.0.15 FoundationDB Java driver version. 

```scala
libraryDependencies += // FoundationDB Java driver dependency is required
libraryDependencies += "me.archdev" %% "foundation-db-scala" % "0.1.5"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
```

## Work in progress
- [ ] Testkit support for onError, mutate, watch, getVersiontimestamp, getReadVersion, setReadVersion, getCommittedVersion
- [ ] Docs 

## Copyright
Copyright (C) 2018 Arthur Kushka.  
Distributed under the MIT License.

## Contact
Wanna ask me something or stay in touch?   
Follow my Twitter [@arhelmus](https://twitter.com/Arhelmus)
