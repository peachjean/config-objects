package net.peachjean.confobj.support.example;

import net.peachjean.confobj.ConfigObject;
import net.peachjean.confobj.ImplementationKey;

@ConfigObject
public interface PluginParentCO {
    Integer getCount();

    public static abstract class Defaults implements PluginParentCO {

    }
}
