Yaml
====

[![Build Status](https://www.travis-ci.org/edadma/yaml.svg?branch=master)](https://www.travis-ci.org/edadma/yaml)
[![Build status](https://ci.appveyor.com/api/projects/status/h5b23n2vd0k4oh9q/branch/master?svg=true)](https://ci.appveyor.com/project/edadma/yaml/branch/master)
[![Coverage Status](https://coveralls.io/repos/github/edadma/yaml/badge.svg?branch=master)](https://coveralls.io/github/edadma/yaml?branch=master)
[![License](https://img.shields.io/badge/license-ISC-blue.svg)](https://github.com/edadma/yaml/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/latest_release-v0.1.4-orange.svg)](https://github.com/edadma/yaml/releases/tag/v0.1.4)

*yaml* is a parser for the [YAML](http://yaml.org/) data serialization language written in [Scala](http://scala-lang.org). *yaml* is moving towards compliance with [YAML Version 1.2](http://yaml.org/spec/1.2/spec.html), but is still in early development.


Examples
--------

Here are some example code snippets showing how to use the parser.

### Short Example

This is from [example 2.10](http://yaml.org/spec/1.2/spec.html#id2760658) from chapter 2 of the spec.

```scala
import xyz.hyperreal.yaml._

object Example extends App {

  val result =
    read(
      """
        |---
        |hr:
        |  - Mark McGwire
        |  # Following node labeled SS
        |  - &SS Sammy Sosa
        |rbi:
        |  - *SS # Subsequent occurrence
        |  - Ken Griffey
      """.stripMargin
    )

  println( result )

}
```

#### Output

    List(Map(hr -> List(Mark McGwire, Sammy Sosa), rbi -> List(Sammy Sosa, Ken Griffey)))


### Longer Example

This is from [example 2.27](http://yaml.org/spec/1.2/spec.html#id2761823) from chapter 2 of the spec.

```yaml
---
invoice: 34843
date   : 2001-01-23
bill-to: &id001
    given  : Chris
    family : Dumars
    address:
        lines: |
            458 Walkman Dr.
            Suite #292
        city    : Royal Oak
        state   : MI
        postal  : 48046
ship-to: *id001
product:
    - sku         : BL394D
      quantity    : 4
      description : Basketball
      price       : 450.00
    - sku         : BL4438H
      quantity    : 1
      description : Super Hoop
      price       : 2392.00
tax  : 251.42
total: 4443.52
comments:
    Late afternoon is best.
    Backup contact is Nancy
    Billsmer @ 338-4338.
...
```

#### Output

The output from the above example is equivalent the result of the following Scala snippet.

```scala
val bill_to =
  Map(
    "given" -> "Chris",
    "family" -> "Dumars",
    "address" ->
      Map(
        "lines" -> "458 Walkman Dr.\nSuite #292\n",
        "city" -> "Royal Oak",
        "state" -> "MI",
        "postal" -> 48046
      )
  )

  Map(
    "invoice" -> 34843,
    "date" -> LocalDate.parse("2001-01-23"),
    "bill-to" -> bill_to,
    "ship-to" -> bill_to,
    "product" ->
      List(
        Map(
          "sku" -> "BL394D",
          "quantity" -> 4,
          "description" -> "Basketball",
          "price" -> 450.00
        ),
        Map(
          "sku" -> "BL4438H",
          "quantity" -> 1,
          "description" -> "Super Hoop",
          "price" -> 2392.00
        )
      ),
    "tax" -> 251.42,
    "total" -> 4443.52,
    "comments" -> "Late afternoon is best. Backup contact is Nancy Billsmer @ 338-4338."
  )
```

Usage
-----

Use the following definition to use Yaml in your Maven project:

```xml
<repository>
  <id>hyperreal</id>
  <url>https://dl.bintray.com/edadma/maven</url>
</repository>

<dependency>
  <groupId>xyz.hyperreal</groupId>
  <artifactId>yaml</artifactId>
  <version>0.1.4</version>
</dependency>
```

Add the following to your `build.sbt` file to use *yaml* in your SBT project:

```sbt
resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies += "xyz.hyperreal" %% "yaml" % "0.1.4"
```

Building
--------

### Requirements

- Java 8
- SBT 1.1.6+
- Scala 2.12.6+

### Clone and Assemble Executable

```bash
git clone git://github.com/edadma/yaml.git
cd yaml
sbt assembly
```

The command `sbt assembly` also runs all the unit tests.


License
-------

ISC © 2018 Edward A. Maxedon, Sr.