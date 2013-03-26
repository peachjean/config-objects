package net.peachjean.confobj.support.example;

import net.peachjean.confobj.ConfigObject;

import java.util.List;
import java.util.Set;

@ConfigObject
public interface GenericConfigObject {
    public String getString();

    public List<String> getAllStrings();

    public Set<String> getStringSet();

    public static abstract class Defaults implements GenericConfigObject {}
}
