package net.peachjean.confobj.support;

import org.apache.commons.configuration.Configuration;

public interface ConfigObjectFactory {
    <T> T create(Configuration config, Class<T> confObjType);

    <T> T create(Configuration config, Class<T> confObjType, InstantiationContext context);

    <T> T create(Configuration config, Class<T> confObjType, Object context);

    <T> Instantiator<T> createGenerator(Class<T> confObjType);
}
