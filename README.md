ITSCO
=====
Immutable, type-safe configuration objects (this name may change, it doesn't seem to describe all usecases)

This project has the goal of encapsulating several different, related patterns that I find myself using (and recreating)
in multiple projects.  The idea is to separate the developer from the parsing, to locate configuration/parsing
information as close to the applicable code as possible, and to enable discoverability APIs on available configuration
parameters and structure fields.

Basic Use
---------

The application developer defines a configuration object as an interface of getter methods.  He also defines an inner
static abstract class of this interface, that implements this interface, named 'Defaults'.  The return value of any
methods implemented in 'Defaults' will be treated as default values.  Any methods that do not have default values will
be treated as required. Here is an example:

    public interface MyConfig {
      /* a parameter named 'value1' */
      String getValue1();
      /* a parameter named 'value2' */
      String getValue2();
      /* a parameter named 'intValue' */
      Integer getIntValue();

      public static abstract class Defaults implements MyConfig {
        public String getValue2() {
          return "second value";
        }
        public Integer getValue() {
          return 42;
        }
      }
    }

This class could map the properties file (myconfig.properties):

    value1=My Configured Value
    intValue=88

The developer would then create an `ItscoFactory` and create instances of MyConfig from it:

    ItscoFactory<Configuration> configurationFactory = new ConfigurationItscoFactory();
    Configuration config = new PropertiesConfiguration(new File("myconfig.properties"));
    MyConfig myConfig = configurationFactory.create(config, MyConfig.class);
    // alternatively, a function can be created
    Function<Configuration, MyConfig> configFunction = configurationFactory.createGenerator(MyConfig.class);
    MyConfig myConfig2 = configFunction.apply(config);

    // prints "My Configured Value"
    System.out.println(myConfig.getValue1());

    // prints "second value"
    System.out.println(myConfig.getValue2());

    // prints "42'
    System.out.println(myConfig.getIntValue());

Use Cases
---------

1. Application Configuration
2. Communication Payloads (ex. body content for JAXRS)
3. Object marshalling/unmarshalling for storage and payload - a combination of 1 & 2.

###Use Case 1
Ever application has some sort of configuration. This configuration is often don in many different ways -- properties
files, ini files, xml files, json files, cli options, database tables, etc.  In the application, this configuration is
often looked up with a simple string->string map or a custom parser is written.  As an app grows, one of two things
happen: either

 1. the configuration keys get dispersed all over the application, and knowing the full set or doing any sort of
    analysis on the configuration becomes near-impossible OR
 2. the configuration key s are all stored as constants in a single class, causing tight coupling snd circular semantic
    dependencies

Apache's commons-configuration has done a good job of abstracting the different methods of specification and making them
available to the application programmer as a standard interface. However, it has done nothing to address the
proliferation and coupling of configuration keys.

###Use Case 2
Applications that communicate:  think JAXRS - objects are POSTed and returns as structured text.  We often end up
writing multitudes of custom parsing and marshalling. Existing automated marshallers rely heavily on setters - making
these objects very mutable.  A message sent across the wire is not mutable, making this mutability merely an artifact
of the framework. In addition, these objects can often benefit from the Builder pattern. However, a comprehensive
builder can be verbose, rote, and full of boilerplate.  These things tend to require significant attention, or encourage
a lack of functionality

###Use Case 3
You have a structure object that represents some process. Client A will submit using a json API, Client B will submit in
xml, you submit to worker agents using java objects, you store the object in a RDBMS.  You need the same validation,
structure, and type safety in all of these places.  Think of this as a sort of merging of 1 & 2 -
"Communicated Configuration"

Non-Obvious Needs
-----------------

1. Field-type supporting exposing sets of properties as key/value pairs - used for supporting embedding configuration
   that will be passed to other libraries
2. Subtype of field B determined by field A OR subtype of field A determined by a key in A - this supports configuration
   that defines a specific implementation to be used when specific implementations have different configuration objects.
3. Support changes between versions of itsco interfaces
    * adding a field, removing a field, changing a type, changing a name, changing cardinality
    * testing that old versions are still compatible
4. Discoverability - shall be able to dynamically describe available configuration
5. Listeners on configuration change - some configuration will be used to create other objects - when the configuration
   changes, these objects will need to be modified
6. Schema generators - should be able to generate an xml schema, json schema, db schema, etc based on the itsco.
7. Should support JSR-303 annotations on getters.

Itsco Supporting Features
-------------------------

Planned features, with supporting use cases

* Build-time generated Builder class that allows copying
    * (2) - instantiate for returning in APIs
    * (3) - clients instantiate
* Configuration-backed, runtime-generated, dynamic implementations
    * (1) - load configs as objects and keep the objects up-to-date as the configuration changes
* Runtime-generated unmarshalled implementation
    * (2) - recreating objects on receiving side
* Runtime-generated marshaller
    * (2) - sending side object to stream conversion
* Support config-keyed subclasses of itsco fields where parents don't need to know about the impls
    * (1) - one key specifies an impl, each impl has its own configuration, impl can be plugins that are deployed with no
          knowledge by the parent
* Pluggable backing mechanisms
    * (3) - user has custom format or DB Schema
* Pluggable type conversions
    * (1,2,3) - user has custom types that need to be supported
* Support "valueOf" static methods
* Support "Optional" type (from Guava)
    * (1) - sometimes a default doesn't cut it - we want to know that it is unspecified
* Support Collections (Map, List, Collection (list), Iterable (list), Set, SortedSet, NavigableSet, Multiset,
                       Multimap, Table)
