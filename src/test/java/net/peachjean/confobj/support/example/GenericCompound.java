package net.peachjean.confobj.support.example;

import net.peachjean.confobj.ConfigObject;

import java.util.List;

@ConfigObject
public interface GenericCompound {
    String getName();

    List<String> getRoles();

    List<Integer> getLimits();

    GenericCompoundChild getChild();

    public static abstract class Defaults implements GenericCompound {

    }
}
