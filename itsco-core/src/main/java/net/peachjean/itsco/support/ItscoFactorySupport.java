package net.peachjean.itsco.support;

import com.google.common.base.Function;
import net.peachjean.itsco.Itsco;

/**
 *
 */
public abstract class ItscoFactorySupport<C> implements ContextAccessor<C> {

    private final Instantiator instantiator = new Instantiator();
    private final FieldResolutionStrategy[] strategies = {
            StringResolutionStrategy.INSTANCE,
            ValueOfResolutionStrategy.INSTANCE,
            new ItscoResolutionStrategy()
    };


    protected ItscoFactorySupport() {
    }

    public <T> T create(C context, Class<T> itscoClass) {
        return instantiator.lookupFunction(itscoClass).apply(createBacker(context));
    }

    public <T> Function<C, T> createGenerator(final Class<T> itscoClass)
    {
        return new Function<C, T>() {
            final Function<ItscoBacker, T> implFunction = instantiator.lookupFunction(itscoClass);

            @Override
            public T apply(final C context) {
                return implFunction.apply(createBacker(context));
            }
        };
    }

    private ItscoBacker createBacker(final C context) {
        return new ItscoBacker() {
            public <T> T lookup(final String name, final Class<T> lookupType) {
                FieldResolutionStrategy resolutionStrategy = determineStrategy(lookupType);
                final T resolved = resolutionStrategy.resolve(name, lookupType, context, ItscoFactorySupport.this);
                if(resolved == null)
                {
                    throw new IllegalStateException("No value for " + name);
                }
                return resolved;
            }

            public <T> T lookup(final String name, final Class<T> lookupType, final T defaultValue) {
                FieldResolutionStrategy resolutionStrategy = determineStrategy(lookupType);
                final T resolved = resolutionStrategy.resolve(name, lookupType, context, ItscoFactorySupport.this);
                return resolved != null ? resolved : defaultValue;
            }

            private <T> FieldResolutionStrategy determineStrategy(final Class<T> lookupType) {
                for(FieldResolutionStrategy strategy: strategies)
                {
                    if(strategy.supports(lookupType))
                    {
                        return strategy;
                    }
                }
                throw new IllegalStateException("No strategy to support type " + lookupType.getName());
            }
        };
    }

    private class ItscoResolutionStrategy implements FieldResolutionStrategy {
        @Override
        public <T, CN> T resolve(final String name, final Class<T> lookupType, final CN context, final ContextAccessor<CN> contextAccessor) {
            if(!this.supports(lookupType))
            {
                throw new IllegalArgumentException("This strategy only supports itsco types, not " + lookupType.getName());
            }

            @SuppressWarnings("unchecked")
            C subContext = (C) contextAccessor.subContextLookup(context, name);
            return ItscoFactorySupport.this.create(subContext, lookupType);
        }

        @Override
        public boolean supports(final Class<?> lookupType) {
            return lookupType.isAnnotationPresent(Itsco.class);
        }
    }
}
