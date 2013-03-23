package net.peachjean.confobj.support.example.shared;

import net.peachjean.confobj.ConfigObject;

@ConfigObject
public interface SharedItsco {

    String getNamespace();

    Integer getMaxSize();

    public static abstract class Defaults implements SharedItsco {

    }
}
