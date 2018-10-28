FoundationDB Scala client
=========================

This is a functional Scala wrapper for Java FoundationDB driver.
Goal of it is to provide easy to use access to FoundationDB from Scala.
**If you are using FoundationDB in your Scala project already, please [contact me](https://archdev.me).**   

Work in progress:
- [x] Transaction DSL concept
- [x] Generic serialization
- [x] Namespaces control
- [ ] Integration and Unit tests
- [ ] Directory management
- [ ] Optimization of async effects
- [ ] Full support of transaction DSL 
- [ ] Public CI setup and release

## Example
Current example is a design concept, if you have ideas how to make it better, feel free to open an Issue or PR.
```scala
  val fdb = FoundationDB(520)

  implicit val subspace: Subspace = Subspace("test_subspace")
  
  fdb.exec(db =>
      for {
        _ <- db.set("key", SomeStorageModel("1", 2, "3"))
        _ <- db.set("key2", "value2")
        result <- db.get[String, SomeStorageModel]("key")
        _ <- db.delete("key2")
      } yield result
  )
```

## Copyright
Copyright (C) 2018 Arthur Kushka.  
Distributed under the MIT License.

## Contact
Wanna ask me something or stay in touch?   
Follow my Twitter [@arhelmus](https://twitter.com/Arhelmus)
