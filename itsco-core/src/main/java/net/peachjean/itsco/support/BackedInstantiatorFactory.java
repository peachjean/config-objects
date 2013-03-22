package net.peachjean.itsco.support;

import javassist.*;

import java.util.*;

class BackedInstantiatorFactory {
    private final Map<Class, BackedInstantiator<?>> cache = new HashMap<Class, BackedInstantiator<?>>();
    private final ImplementationGenerator implementationGenerator = new InlineImplementationGenerator();

    @SuppressWarnings("unchecked")
    <T> BackedInstantiator<T> lookupFunction(final Class<T> itscoInterface) {
        if (!cache.containsKey(itscoInterface)) {
            synchronized (cache) {
                if (!cache.containsKey(itscoInterface)) {
                    try {
                        cache.put(itscoInterface, createInstantiatior(itscoInterface));
                    } catch (NotFoundException e) {
                        throw new RuntimeException("Could not create instantiation function for " + itscoInterface.getName(), e);
                    } catch (CannotCompileException e) {
                        throw new RuntimeException("Could not create instantiation function for " + itscoInterface.getName(), e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("Could not create instantiation function for " + itscoInterface.getName(), e);
                    }
                }
            }
        }
        return (BackedInstantiator<T>) cache.get(itscoInterface);
    }

    private <T> BackedInstantiator<T> createInstantiatior(final Class<T> itscoClass) throws NotFoundException, CannotCompileException, NoSuchMethodException {
        final Class<? extends T> implClass = implementationGenerator.implement(itscoClass);

        return new BackedInstantiatorImpl<T>(itscoClass, implClass);
    }
}
