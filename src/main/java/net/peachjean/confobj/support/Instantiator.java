package net.peachjean.confobj.support;

import org.apache.commons.configuration.Configuration;

public interface Instantiator<T> {
    T instantiate(Configuration configuration);

    T instantiate(Configuration configuration, InstantiationContext context);

    T instantiate(Configuration configuration, Object context);
}
