package net.peachjean.confobj.support.example;

/**
 * Description of file content.
 *
 * @author jbunting
 *         3/30/13
 */
public interface PluginOneCO extends PluginParentCO {
    boolean isLate();

    Integer getMaxInstances();

    public static abstract class Defaults implements PluginOneCO {

    }
}
