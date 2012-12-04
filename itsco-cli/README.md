How to use itsco-cli
====================

Purpose
-------

The primary goal is to make it easy to use command line arguments as part of the source for building up an ITSCO object.
To this end, it has two primary functionalities:

1. Generate a commons-cli `Options` object from an itsco interface.
2. Map the commons-cli `CommandLine` object parsed from the `Options` object in (1) to a commons-configuration
   `Configuration` object.

Mechanisms
----------

itsco-cli exposes the two interface points `CliItscoParserFactory` and `CliItscoParser`.  `CliItscoParserFactory`
provides the method `CliItscoParser generate(Class<?> itscoType)`.


itsco-cli exposes two primary interface points - `CliItscoFactory` and `CliItscoParser`.  In addition, it defines the
data structure `ParsedOptions`.  `CliItscoFactory` implements `ItscoFactory<ParsedOptions>`, while `CliItscoParser`
provides the method `ParsedOptions parse(Class<?> itscoType, String[] args)`.  Typically, we can achieve CLI->Itsco
transformation by invoking `cliItscoFactory.create(cliItscoParser.parse(MyItsco.class, args), MyItsco.class)`.
