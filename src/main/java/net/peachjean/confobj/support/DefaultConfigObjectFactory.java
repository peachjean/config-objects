package net.peachjean.confobj.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.peachjean.confobj.introspection.BaseConfigObjectVisitor;
import net.peachjean.confobj.introspection.ConfigObjectIntrospector;
import net.peachjean.confobj.introspection.GenericType;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.configuration.Configuration;

/**
 *
 */
public class DefaultConfigObjectFactory implements ConfigObjectFactory {

    private final ImplementationGenerator implementationGenerator = new AsmImplementationGenerator();

    private final List<FieldResolutionStrategy> strategies;

    private final FieldResolutionStrategy.Determiner strategyDeterminer =  new FieldResolutionStrategy.Determiner() {
        @Override
        public FieldResolutionStrategy determineStrategy(GenericType<?> type) {
            FieldResolutionStrategy frs = this.findStrategy(type);
            if(frs == null) {
                throw new IllegalStateException("No strategy to support type " + type.getRawType().getName());
            } else {
                return frs;
            }
        }

        @Override
        public boolean isStrategyAvailable(GenericType<?> type) {
            return this.findStrategy(type) != null;
        }

        private FieldResolutionStrategy findStrategy(GenericType<?> type) {
            for (FieldResolutionStrategy strategy : strategies) {
                if (strategy.supports(type)) {
                    return strategy;
                }
            }
            return null;
        }
    };

	private FieldResolutionStrategy wrapToCast(final FieldResolutionStrategy delegateStrategy)
	{
		return new FieldResolutionStrategy()
		{
			@Override
			public <T, C> FieldResolution<T> resolve(final String name, final GenericType<T> type, final Configuration context, final C resolutionContext)
			{
				final FieldResolution<T> delegate = delegateStrategy.resolve(name, type, context, resolutionContext);
				return new FieldResolution<T>()
				{
					@Override
					public T resolve() throws MissingConfigurationException
					{
						if (context.containsKey(name) && type.getRawType().isInstance(context.getProperty(name)))
						{
							return type.getRawType().cast(context.getProperty(name));
						}
						else
						{
							return delegate.resolve();
						}
					}

					@Override
					public T resolve(final T defaultValue)
					{
						if (context.containsKey(name) && type.getRawType().isInstance(context.getProperty(name)))
						{
							return type.getRawType().cast(context.getProperty(name));
						}
						else
						{
							return delegate.resolve(defaultValue);
						}
					}
				};
			}

			@Override
			public boolean supports(final GenericType<?> lookupType)
			{
				return delegateStrategy.supports(lookupType);
			}
		};
	}

	private final SimpleCache<Class<?>, Instantiator<?>> instantiatorCache = SimpleCache.build(new SimpleCache.Loader<Class<?>, Instantiator<?>>() {
        @Override
        public Instantiator<?> load(Class<?> key) {
            return DefaultConfigObjectFactory.this.createNewInstantiator(key);
        }
    });

    private final SimpleCache<Class<?>, Map<String, Class<?>>> implementationCache = SimpleCache.build(new SimpleCache.Loader<Class<?>, Map<String, Class<?>>>() {
        @Override
        public Map<String, Class<?>> load(Class<?> key) {
            return (Map<String, Class<?>>)DefaultConfigObjectFactory.this.createNewImplementationMap(key);
        }
    });

    public DefaultConfigObjectFactory() {
        this(Collections.<FieldResolutionStrategy>emptyList());
    }

    public DefaultConfigObjectFactory(FieldResolutionStrategy ... strategies) {
        this(Arrays.asList(strategies));
    }

    public DefaultConfigObjectFactory(Iterable<FieldResolutionStrategy> strategies) {
        List<FieldResolutionStrategy> strategyList = new ArrayList<FieldResolutionStrategy>();
        strategyList.add(StringResolutionStrategy.INSTANCE);
        strategyList.add(wrapToCast(ValueOfResolutionStrategy.INSTANCE));
	    strategyList.add(wrapToCast(JodaConvertResolutionStrategy.INSTANCE));
        strategyList.add(new ConfigObjectResolutionStrategy(this));
        strategyList.add(ConfigurationResolutionStrategy.INSTANCE);
        strategyList.add(new ListResolutionStrategy());
        strategyList.add(new SetResolutionStrategy());
        strategyList.add(new MapResolutionStrategy());
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
    public <T> T createNamedImpl(Configuration config, Class<T> confObjType, String name) {
        return this.create(config, lookupNamedImpl(confObjType, name));
    }

    @Override
    public <T> T createNamedImpl(Configuration config, Class<T> confObjType, String name, InstantiationContext context) {
        return this.create(config, lookupNamedImpl(confObjType, name), context);
    }

    @Override
    public <T> T createNamedImpl(Configuration config, Class<T> confObjType, String name, Object context) {
        return this.create(config, lookupNamedImpl(confObjType, name), context);
    }

    private <T> Class<? extends T> lookupNamedImpl(Class<T> confObjType, String name) {
        Map<String, Class<?>> implMap = this.implementationCache.get(confObjType);
        if(implMap.containsKey(name)) {
            return (Class<? extends T>) implMap.get(name);
        } else {
            throw new UnrecognizedImplementationNameException(name, confObjType);
        }
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

            private ConfigObjectBacker createBacker(final Configuration context) {
                return new ConfigurationConfigObjectBacker(context, strategyDeterminer);
            }
        };
    }

    private <T> Map<String, Class<?>> createNewImplementationMap(Class<T> key) {
        Map<String, Class<?>> implementationMap = new HashMap<String, Class<?>>();
        ConfigObjectIntrospector.visitMembers(key, implementationMap, new BaseConfigObjectVisitor<T, Map<String, Class<?>>>() {
            @Override
            public void visitNamedImplementation(Class<? extends T> implementationClass, String name, Map<String, Class<?>> input) {
                input.put(name, implementationClass);
            }
        });
        return implementationMap;
    }
}
