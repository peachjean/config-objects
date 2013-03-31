package net.peachjean.confobj.support;

/**
 * Description of file content.
 *
 * @author jbunting
 *         3/31/13
 */
public class MissingConfigurationException extends RuntimeException {
    public MissingConfigurationException(String configurationName) {
        super(String.format("There is no configuration defined at %s.", configurationName));
    }
}
