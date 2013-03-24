Config Objects
==============

This project is part of an evoluation over multiple iterations in my quest to improve the state of configuration in
object-oriented Java applications.

Problems Addressed
------------------

1. Type Safety
    Configuration parameters are not provided to the classes that use them in a type-safe manner. This means that we end
    up with different parsing mechanisms and inconsistent error messages across our code base.
2. Validation
    Oftentimes, in addition to a type requirement, properties also must pass certain validation criteria. Perhaps we are
    looking for a number from 1 to 10, or a 6-10 character string.
3. Consistent Error Reporting
    When the code for reading in, parsing, and validating properties is scattered across our codebase, we end up with a
    multitude of error messages and states for the exact same sort of failure.
4. Discoverability
    If an application has users, then users need to configure it. This means that we need to document our configuration.
    I frequently see undocumented parameters, out of date documentation, and a lack of type or validation information
    for configuration parameters.
5. Dynamicism
    The majority of configuration patterns that I see don't have any allowance for configuration to change during the
    application's lifecycle. This means that we end up with hacks, like continually parsing from `Properties` objects
    or reloading static services.

There are three common patterns that I tend to see for object configuration.

1. Pass a `Properties` or `Configuration` object to a class's constructor. The class then parses the properties that it
   is interested in and uses them.
2. A class defines setters for any configuration parameters that it is interested in. An external framework or utility
   parses configuration files and invokes these setters for relevant properties.
3. A dependency injection framework is used where every value in a properties file is bound as a bean and some form of
   autowiring is used to inject those parameters into the classes that need them.

Approach
--------

This project attempts to define two major components.

1. Pattern
    A pattern for defining configuration objects.
    a. Interface
        The basic approach is that a user will define an interface of getter methods. Each method represents one
        parameter. The types for these methods are typically simple types, supporting the `valueOf()` convention,
        primitives, collections, or other configuration object types. These interfaces should be annotated with
        `@ConfigObject`.
    b. Validation
        Each getter method may define JSR-303 validation annotations.
    c. Documentation
        A basic description of the parameter should be provided in the javadoc for the getter method.
    d. Defaults
        An abstract nested static class named `Defaults` may be defined for the interface. If defined, it should
        implement any getters which should have defaults and return those defaults. Sometimes, default values depend on
        the environment. The `Defaults` class may specify a constructor that gets passed the relevant environment
        objects. JSR-330 `@Qualifier` annotations may be used on these constructor parameters.
2. Implementation Support
    Utilities for working with ConfigObject interfaces.
    a. Introspection
        A basic utility providing visitor-style introspection for a ConfigObject interface.
    b. Implementation
        A utility for dyamically implementing ConfigObject interfaces using [commons-configuration] `Configuration`
        objects as the backing store. The entry point into this is the `ConfigObjectFactory` and the
        `DefaultConfigObjectFactory` implementation.

[commons-configuration]: http://commons.apache.org/configuration/

Basic Use
---------

The application developer defines a configuration object as an interface of getter methods.  He also defines an inner
static abstract class of this interface, that implements this interface, named 'Defaults'.  The return value of any
methods implemented in 'Defaults' will be treated as default values.  Any methods that do not have default values will
be treated as required. Here is an example:

    @ConfigObject
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

The developer would then create a `ConfigObjectFactory` and create instances of MyConfig from it:

    ConfigObjectFactory confObjFactory = new DefaultConfigObjectFactory();
    Configuration config = new PropertiesConfiguration(new File("myconfig.properties"));
    MyConfig myConfig = confObjFactory.create(config, MyConfig.class);
    // alternatively, an instantiator can be created
    Instantiator<MyConfig> configFunction = confObjFactory.createGenerator(MyConfig.class);
    MyConfig myConfig2 = configFunction.instantiate(config);

    // prints "My Configured Value"
    System.out.println(myConfig.getValue1());

    // prints "second value"
    System.out.println(myConfig.getValue2());

    // prints "42'
    System.out.println(myConfig.getIntValue());

Non-Obvious Needs / Future Work
-------------------------------

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
7. Should support JSR-303 annotations on getters.
8. Annotation processor for compile-time validation of `@ConfigObject`-annotated interfaces.
