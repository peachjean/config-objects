package net.peachjean.itsco.support;

import net.peachjean.itsco.Itsco;
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
            new ItscoResolutionStrategy()
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

    private class ItscoResolutionStrategy implements FieldResolutionStrategy {
        @Override
        public <T, CN> T resolve(final String name, final Class<T> lookupType, final Configuration context) {
            if (!this.supports(lookupType)) {
                throw new IllegalArgumentException("This strategy only supports itsco types, not " + lookupType.getName());
            }

            @SuppressWarnings("unchecked")
            Configuration subContext = context.subset (name);
            return DefaultItscoFactory.this.create(subContext, lookupType);
        }

        @Override
        public boolean supports(final Class<?> lookupType) {
            return lookupType.isAnnotationPresent(Itsco.class);
        }

        @Override
        public boolean handlesReloading() {
            return true;
        }
    }

    private class DefaultItscoBacker implements ItscoBacker {

        // contains values that handle reloading on their own - these are often more expensive to create so we
        // don't want to recreate them on every get call
        private final Map<String, Object> cachedValues;
        private final Configuration context;

        public DefaultItscoBacker(Configuration context, Class<?> itscoClass) {
            this.context = context;
            cachedValues = new HashMap<String, Object>();
//            this.
        }

        public <T> T lookup(final String name, final Class<T> lookupType) {
            FieldResolutionStrategy resolutionStrategy = determineStrategy(lookupType);
            if (resolutionStrategy.handlesReloading()) {
                if (cachedValues.containsKey(name)) {
                    return lookupType.cast(cachedValues.get(name));
                }
            }
            final T resolved = resolutionStrategy.resolve(name, lookupType, context);
            if (resolved == null) {
                throw new IllegalStateException("No value for " + name);
            }
            if (resolutionStrategy.handlesReloading()) {
                cachedValues.put(name, resolved);
            }
            return resolved;
        }

        public <T> T lookup(final String name, final Class<T> lookupType, final T defaultValue) {
            FieldResolutionStrategy resolutionStrategy = determineStrategy(lookupType);
            final T resolved = resolutionStrategy.resolve(name, lookupType, context);
            return resolved != null ? resolved : defaultValue;
        }

    }
}
