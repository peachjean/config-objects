package net.peachjean.itsco;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Marks an interface that should be implemented by Itsco.  This interface should contain only bean-compliant getter
 * methods.  Those methods may return primitives, Strings, arrays, Lists, Sets, Maps, Multimaps or other Itsco objects.  The
 * collection classes may specify generics, but the members of those are bound by the same rules.
 *
 * Defaults
 *
 * The interface may optionally have an abstract inner static class that implements this interface, and implements
 * any methods that should have default values.  This class should be named "Defaults".  As an alternative,
 * {@link Defaults} may be specified on the interface to bind a defaults class
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface Itsco {
    boolean generateBuilder() default true;
    String builderName() default "";
}
