package net.peachjean.itsco.support;

import net.peachjean.itsco.Itsco;
import org.apache.commons.configuration.Configuration;

class ItscoResolutionStrategy implements FieldResolutionStrategy {
    private ItscoFactory itscoFactory;

    public ItscoResolutionStrategy(ItscoFactory itscoFactory) {
        this.itscoFactory = itscoFactory;
    }

    @Override
    public <T, C> T resolve(String name, Class<T> lookupType, Configuration configuration, C resolutionContext) {
        if (!this.supports(lookupType)) {
            throw new IllegalArgumentException("This strategy only supports itsco types, not " + lookupType.getName());
        }

        @SuppressWarnings("unchecked")
        Configuration subContext = configuration.subset (name);
        return itscoFactory.create(subContext, lookupType, new ObjectContext(resolutionContext));
    }

    @Override
    public boolean supports(final Class<?> lookupType) {
        return lookupType.isAnnotationPresent(Itsco.class);
    }

    @Override
    public boolean isContextBacked() {
        return true;
    }
}
