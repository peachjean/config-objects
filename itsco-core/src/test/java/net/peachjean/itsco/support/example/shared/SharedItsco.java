package net.peachjean.itsco.support.example.shared;

import net.peachjean.itsco.Itsco;

@Itsco
public interface SharedItsco {

    String getNamespace();

    Integer getMaxSize();

    public static abstract class Defaults implements SharedItsco {

    }
}
