package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class DefaultConfigObjectFactory implements ConfigObjectFactory {

    private final ImplementationGenerator implementationGenerator = new AsmImplementationGenerator();

    private final List<FieldResolutionStrategy> strategies;

    private final FieldResolutionStrategy.Determiner strategyDeterminer =  new FieldResolutionStrategy.Determiner() {
        @Override
        public FieldResolutionStrategy determineStrategy(GenericType<?> type) {
            for (FieldResolutionStrategy strategy : strategies) {
                if (strategy.supports(type)) {
                    return strategy;
                }
            }
            throw new IllegalStateException("No strategy to support type " + type.getRawType().getName());
        }
    };

    private final SimpleCache<Class<?>, Instantiator<?>> instantiatorCache = SimpleCache.build(new SimpleCache.Loader<Class<?>, Instantiator<?>>() {
        @Override
        public Instantiator<?> load(Class<?> key) {
            return DefaultConfigObjectFactory.this.createNewInstantiator(key);
        }
    });

    public DefaultConfigObjectFactory() {
        this(Collections.<FieldResolutionStrategy>emptyList());
    }

    public DefaultConfigObjectFactory(FieldResolutionStrategy ... strategies) {
        this(Arrays.asList(strategies));
    }

    public DefaultConfigObjectFactory(Iterable<FieldResolutionStrategy> strategies) {
        List<FieldResolutionStrategy> strategyList = new ArrayList();
        strategyList.add(StringResolutionStrategy.INSTANCE);
        strategyList.add(ValueOfResolutionStrategy.INSTANCE);
        strategyList.add(new ConfigObjectResolutionStrategy(this));
        strategyList.add(ConfigurationResolutionStrategy.INSTANCE);
        strategyList.add(new ListResolutionStrategy());
        // add defaults above this comment
        for(FieldResolutionStrategy strategy: strategies) {
            strategyList.add(strategy);
        }
        for(FieldResolutionStrategy strategy: strategyList) {
            if(strategy instanceof FieldResolutionStrategy.RequiresDeterminer) {
                ((FieldResolutionStrategy.RequiresDeterminer)strategy).setDeterminer(this.strategyDeterminer);
            }
        }
        this.strategies = UnmodifiableList.unmodifiableList(strategyList);
    }

    @Override
    public <T> T create(Configuration config, Class<T> confObjType) {
        return createGenerator(confObjType).instantiate(config);
    }

    @Override
    public <T> T create(Configuration config, Class<T> confObjType, InstantiationContext context) {
        return createGenerator(confObjType).instantiate(config, context);
    }

    @Override
    public <T> T create(Configuration config, Class<T> confObjType, Object context) {
        return create(config, confObjType, new ObjectContext(context));
    }

    @Override
    public <T> Instantiator<T> createGenerator(final Class<T> confObjType) {
        return (Instantiator<T>) instantiatorCache.get(confObjType);
    }

    private <T> Instantiator<T> createNewInstantiator(final Class<T> confObjType) {
        final Class<? extends T> implClass = implementationGenerator.implement(confObjType);

        final BackedInstantiator<T> instantiatior = new BackedInstantiatorImpl<T>(confObjType, implClass);

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

            private <T> ConfigObjectBacker createBacker(final Configuration context) {
                return new ConfigurationConfigObjectBacker(context, strategyDeterminer);
            }
        };
    }

}
