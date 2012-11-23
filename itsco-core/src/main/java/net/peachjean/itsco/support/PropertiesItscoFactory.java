package net.peachjean.itsco.support;

import java.util.Properties;

public class PropertiesItscoFactory extends ItscoFactorySupport<Properties> {

    public PropertiesItscoFactory() {
    }

    @Override
    protected boolean contains(final Properties context, final String key) {
        return context.getProperty(key) != null;
    }

    @Override
    protected String contextLookup(final Properties context, final String key) {
        return context.getProperty(key);
    }
}
