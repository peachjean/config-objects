package net.peachjean.confobj.support;

/**
 * Description of file content.
 *
 * @author jbunting
 *         3/31/13
 */
public class UnrecognizedImplementationNameException extends RuntimeException {
    public UnrecognizedImplementationNameException(String name, Class<?> configObjectClass) {
        super(String.format("Could not locate implementation '%s' of config object type '%s'.", name, configObjectClass.getName()));
    }
}
