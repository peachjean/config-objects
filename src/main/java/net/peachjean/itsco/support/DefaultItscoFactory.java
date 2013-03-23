package net.peachjean.itsco.support;

import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DefaultItscoFactory implements ItscoFactory {

    private final BackedInstantiatorFactory backedInstantiatorFactory = new BackedInstantiatorFactory();
    private final FieldResolutionStrategy[] strategies = {
            StringResolutionStrategy.INSTANCE,
            ValueOfResolutionStrategy.INSTANCE,
            new ItscoResolutionStrategy(this)
    };


    protected DefaultItscoFactory() {
    }

    @Override
    public <T> T create(Configuration config, Class<T> itscoClass) {
        return backedInstantiatorFactory.lookupFunction(itscoClass).instantiate(createBacker(config, itscoClass));
    }

    @Override
    public <T> T create(Configuration config, Class<T> itscoClass, InstantiationContext context) {
        return backedInstantiatorFactory.lookupFunction(itscoClass).instantiate(createBacker(config, itscoClass), context);
    }

    @Override
    public <T> Instantiator<T> createGenerator(final Class<T> itscoClass) {
        return new Instantiator<T>() {
            @Override
            public T instantiate(Configuration configuration) {
                return create(configuration, itscoClass);
            }

            @Override
            public T instantiate(Configuration configuration, InstantiationContext context) {
                return create(configuration, itscoClass, context);
            }
        };
    }

    private <T> ItscoBacker createBacker(final Configuration context, Class<T> itscoClass) {
        return new DefaultItscoBacker(context, itscoClass);
    }

    protected <T> FieldResolutionStrategy determineStrategy(final Class<T> lookupType) {
        for (FieldResolutionStrategy strategy : strategies) {
            if (strategy.supports(lookupType)) {
                return strategy;
            }
        }
        throw new IllegalStateException("No strategy to support type " + lookupType.getName());
    }

    private class DefaultItscoBacker<I> implements ItscoBacker<I> {

        // contains values that handle reloading on their own - these are often more expensive to create so we
        // don't want to recreate them on every get call
        private final Map<String, Object> cachedValues;
        private final Configuration context;
        private I containing;

        public DefaultItscoBacker(Configuration context, Class<I> itscoClass) {
            this.context = context;
            cachedValues = new HashMap<String, Object>();
//            this.
        }

        @Override
        public void setContaining(I containing) {
            this.containing = containing;
        }

        @Override
        public <T> T lookup(final String name, final Class<T> lookupType) {
            this.validateState();
            FieldResolutionStrategy resolutionStrategy = determineStrategy(lookupType);
            if (resolutionStrategy.isContextBacked()) {
                if (cachedValues.containsKey(name)) {
                    return lookupType.cast(cachedValues.get(name));
                }
            }
            final T resolved = resolutionStrategy.resolve(name, lookupType, context, containing);
            if (resolved == null) {
                throw new IllegalStateException("No value for " + name);
            }
            if (resolutionStrategy.isContextBacked()) {
                cachedValues.put(name, resolved);
            }
            return resolved;
        }

        @Override
        public <T> T lookup(final String name, final Class<T> lookupType, final T defaultValue) {
            this.validateState();
            FieldResolutionStrategy resolutionStrategy = determineStrategy(lookupType);
            final T resolved = resolutionStrategy.resolve(name, lookupType, context, containing);
            return resolved != null ? resolved : defaultValue;
        }

        private void validateState() {
            if(this.containing == null) {
                throw new IllegalStateException("A containing object must be set before the backer is usable.");
            }
        }

    }
}
