package net.peachjean.itsco.support;

import org.apache.commons.configuration.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class DefaultItscoFactory implements ItscoFactory {

    private final ImplementationGenerator implementationGenerator = new AsmImplementationGenerator();

    private final List<FieldResolutionStrategy> strategies = Arrays.asList(
            StringResolutionStrategy.INSTANCE,
            ValueOfResolutionStrategy.INSTANCE,
            new ItscoResolutionStrategy(this)
    );

    private final SimpleCache<Class<?>, Instantiator<?>> instantiatorCache = SimpleCache.build(new SimpleCache.Loader<Class<?>, Instantiator<?>>() {
        @Override
        public Instantiator<?> load(Class<?> key) {
            return DefaultItscoFactory.this.createNewInstantiator(key);
        }
    });

    protected DefaultItscoFactory() {
    }

    @Override
    public <T> T create(Configuration config, Class<T> itscoClass) {
        return createGenerator(itscoClass).instantiate(config);
    }

    @Override
    public <T> T create(Configuration config, Class<T> itscoClass, InstantiationContext context) {
        return createGenerator(itscoClass).instantiate(config, context);
    }

    @Override
    public <T> T create(Configuration config, Class<T> itscoClass, Object context) {
        return create(config, itscoClass, new ObjectContext(context));
    }

    @Override
    public <T> Instantiator<T> createGenerator(final Class<T> itscoClass) {
        return (Instantiator<T>) instantiatorCache.get(itscoClass);
    }

    private <T> Instantiator<T> createNewInstantiator(final Class<T> itscoClass) {
        final Class<? extends T> implClass = implementationGenerator.implement(itscoClass);

        final BackedInstantiator<T> instantiatior = new BackedInstantiatorImpl<T>(itscoClass, implClass);

        return new Instantiator<T>() {

            @Override
            public T instantiate(Configuration configuration) {

                return instantiatior.instantiate(createBacker(configuration));
            }

            @Override
            public T instantiate(Configuration configuration, InstantiationContext context) {
                return instantiatior.instantiate(createBacker(configuration), context);
            }

            @Override
            public T instantiate(Configuration configuration, Object context) {
                return this.instantiate(configuration, new ObjectContext(context));
            }

            private <T> ItscoBacker createBacker(final Configuration context) {
                return new ConfigurationItscoBacker(context, strategies);
            }
        };
    }

}
