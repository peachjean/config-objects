package net.peachjean.confobj;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Description of file content.
 *
 * @author jbunting
 *         3/30/13
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
public @interface ImplementationKey {
}
