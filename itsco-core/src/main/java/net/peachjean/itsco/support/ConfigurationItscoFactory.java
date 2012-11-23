package net.peachjean.itsco.support;

import org.apache.commons.configuration.Configuration;

public class ConfigurationItscoFactory extends ItscoFactorySupport<Configuration> {
    @Override
    public boolean contains(final Configuration context, final String key) {
        return context.containsKey(key);
    }

    @Override
    public String contextLookup(final Configuration context, final String key) {
        return context.getString(key);
    }

    @Override
    public Configuration subContextLookup(final Configuration context, final String key) {
        return context.subset(key);
    }
}
