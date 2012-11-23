package net.peachjean.itsco.support;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javassist.*;
import javassist.bytecode.AccessFlag;
import org.apache.commons.lang.RandomStringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

class Instantiator {

    private final LoadingCache<Class, Function<ItscoBacker, Object>> cache = CacheBuilder.newBuilder().build(new ImplementationGenerator());

    @SuppressWarnings("unchecked")
    <T> Function<ItscoBacker, T> lookupFunction(final Class<T> itscoInterface) {
        try {
            return (Function<ItscoBacker, T>) cache.get(itscoInterface);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to instantiate itsco " + itscoInterface.getName(), e);
        }
    }

    private class ImplementationGenerator extends CacheLoader<Class,Function<ItscoBacker, Object>> {
        @SuppressWarnings("unchecked")
        @Override
        public Function<ItscoBacker, Object> load(final Class itscoClass) throws Exception {
            return createInstantiationFunction(itscoClass);
        }

        private <T> Function<ItscoBacker, T> createInstantiationFunction(final Class<T> itscoClass) throws NotFoundException, CannotCompileException, NoSuchMethodException {
            final Class<? extends T> implClass = createImplClass(itscoClass);

            final Constructor<? extends T> constructor = implClass.getConstructor(ItscoBacker.class);

            return new Function<ItscoBacker, T>() {
                @Override
                public T apply(final ItscoBacker input) {
                    try {
                        return constructor.newInstance(input);
                    } catch (InstantiationException e) {
                        throw new RuntimeException("Failed to invoke constructor for implementation of " + itscoClass.getName(), e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to invoke constructor for implementation of " + itscoClass.getName(), e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Failed to invoke constructor for implementation of " + itscoClass.getName(), e);
                    }
                }
            };
        }

        private <T> Class<? extends T> createImplClass(final Class<T> itscoClass) throws NotFoundException, CannotCompileException {
            ClassPool pool = ClassPool.getDefault();

            // setup class
            CtClass implCC = pool.makeClass(itscoClass.getCanonicalName() + "$$ItscoImpl$$" + RandomStringUtils.randomAlphanumeric(7));
            final CtClass defaultsClass = getDefaultsClass(itscoClass, pool);
            implCC.setSuperclass(defaultsClass);
            final CtClass itscoInterface = pool.getCtClass(itscoClass.getName());
            implCC.setInterfaces(new CtClass[]{itscoInterface});

            // setup field
            CtClass backerType = pool.get(ItscoBacker.class.getName());
            CtField backerField = new CtField(backerType, "backer", implCC);
            backerField.setModifiers(AccessFlag.FINAL);
            implCC.addField(backerField);

            // setup constructor
            final CtConstructor ctConstructor = CtNewConstructor.make(
                    new CtClass[]{backerType},
                    new CtClass[0],
                    "this.backer = $1;",
                    implCC);

            implCC.addConstructor(ctConstructor);

            for(CtMethod method: itscoInterface.getMethods())
            {
                if(isGetter(method))
                {
                    CtMethod defaultsMethod = defaultsClass.getMethod(method.getName(), method.getSignature());
                    String propertyName = determinePropertyName(method);
                    final String returnType = method.getReturnType().getName();
                    String methodBody = defaultsMethod == null || Modifier.isAbstract(defaultsMethod.getModifiers())
                            ? String.format("return (%s) backer.lookup(\"%s\", %s.class);", returnType, propertyName, returnType)
                            : String.format("return (%s) backer.lookup(\"%s\", %s.class, super.%s());", returnType, propertyName, returnType, method.getName());
                    CtMethod implMethod = CtNewMethod.make(
                            method.getModifiers() & ~Modifier.ABSTRACT,
                            method.getReturnType(),
                            method.getName(),
                            new CtClass[0],
                            new CtClass[0],
                            methodBody,
                            implCC);
                    implCC.addMethod(implMethod);
                }
            }

            return classFromCtClass(implCC);
        }

        @SuppressWarnings("unchecked")
        private <T> Class<T> classFromCtClass(final CtClass implCC) throws CannotCompileException {
            return implCC.toClass();
        }

        private boolean isGetter(final CtMethod method) throws NotFoundException {
            return method.getName().startsWith("get")
                    && !"getClass".equals(method.getName())
                    && !"void".equals(method.getReturnType().getName())
                    && method.getParameterTypes().length == 0;
        }

        private String determinePropertyName(final CtMethod method) {
            try {
                if(!isGetter(method))
                {
                    throw new IllegalArgumentException("Method " + method + " is not a valid getter.");
                }
            } catch (NotFoundException e) {
                throw new RuntimeException("Method " + method + " is not a valid getter.", e);
            }
            String name = method.getName();
            String firstChar = name.substring(3, 4);
            String newName = name.substring(4);
            return firstChar.toLowerCase() + newName;
        }

        private <T> CtClass getDefaultsClass(final Class<T> itscoClass, final ClassPool pool) throws NotFoundException {
            for(Class<?> enclosedClass: itscoClass.getClasses())
            {
                if("Defaults".equals(enclosedClass.getSimpleName()))
                {
                    return pool.get(enclosedClass.getName());
                }
            }
            throw new NotFoundException("Could not locate a defaults class for " + itscoClass);
        }


    }
}
