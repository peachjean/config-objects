package net.peachjean.confobj;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks an interface that should be implemented as a config object.  This interface should contain only bean-compliant
 * getter methods.  Those methods may return primitives, Strings, arrays, Lists, Sets, Maps, Multimaps or other ConfigObject
 * objects.  The collection classes may specify generics, but the members of those are bound by the same rules.
 * <p/>
 * Defaults
 * <p/>
 * The interface may optionally have an abstract inner static class that implements this interface, and implements
 * any methods that should have default values.  This class should be named "Defaults".
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface ConfigObject {
    boolean generateBuilder() default true;

    String builderName() default "";
}
