package net.peachjean.itsco.support;

import org.apache.commons.configuration.Configuration;

public interface ItscoFactory {
    <T> T create(Configuration config, Class<T> itscoClass);

    <T> T create(Configuration config, Class<T> itscoClass, InstantiationContext context);

    <T> Instantiator<T> createGenerator(Class<T> itscoClass);
}
