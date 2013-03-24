package net.peachjean.confobj.support.example;

import net.peachjean.confobj.ConfigObject;

import java.util.List;

@ConfigObject
public interface GenericConfigObject {
    public String getString();

    public List<String> getAllStrings();

    public static abstract class Defaults implements GenericConfigObject {}
}
