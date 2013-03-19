package net.peachjean.itsco.support;

import org.apache.commons.configuration.Configuration;

public interface Instantiator<T> {
    T instantiate(Configuration configuration);

    T instantiate(Configuration configuration, InstantiationContext context);
}
