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
- [ ] Integration and Unit tests
- [ ] Directory management
- [ ] Full support of transaction DSL 
- [ ] Public CI setup and release

## Example
Current example is a design concept, if you have ideas how to make it better, feel free to open an Issue or PR.
```scala

  import me.archdev.foundationdb._
  import me.archdev.foundationdb.syntax._

  val fdb = FoundationDB(520)

  implicit val subspace: Subspace = Subspace("test_subspace")
  
  fdb.execute(
      for {
        _ <- set("key", SomeStorageModel("1", 2, "3"))
        _ <- set("key2", "value2")
        result <- get[String, SomeStorageModel]("key")
        _ <- delete("key2")
      } yield result
  )
```

## Copyright
Copyright (C) 2018 Arthur Kushka.  
Distributed under the MIT License.

## Contact
Wanna ask me something or stay in touch?   
Follow my Twitter [@arhelmus](https://twitter.com/Arhelmus)
