package net.peachjean.confobj.support.example;

/**
 * Description of file content.
 *
 * @author jbunting
 *         3/30/13
 */
public interface PluginTwoCO extends PluginParentCO {
    boolean isEarly();

    Integer getMinInstances();

    public static abstract class Defaults implements PluginTwoCO {

    }
}
