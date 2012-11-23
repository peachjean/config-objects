package net.peachjean.itsco.support;

import javassist.*;
import javassist.bytecode.AccessFlag;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**

 * @param <T>
 */
public abstract class ItscoFactorySupport<T,C> {
    private final Class<T> itscoClass;

    protected ItscoFactorySupport(final Class<T> itscoClass) {
        this.itscoClass = itscoClass;
    }

    public T create(C context) {
        try {
            ClassPool pool = ClassPool.getDefault();

            // setup class
            CtClass implCC = pool.makeClass(itscoClass.getCanonicalName() + "$$ItscoImpl");
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
                    String propertyName = determinePropertyName(method.getName());
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

//            implCC.writeFile(new File(".").getAbsolutePath());
            final Class<T> implClass = implCC.toClass();

            return implClass.getConstructor(ItscoBacker.class).newInstance(createBacker(context));
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isGetter(final CtMethod method) throws NotFoundException {
        return method.getName().startsWith("get")
                && !"getClass".equals(method.getName())
                && !"void".equals(method.getReturnType().getName())
                && method.getParameterTypes().length == 0;

    }

    private ItscoBacker createBacker(final C context) {
        return new ItscoBacker() {
            public <T> T lookup(final String name, final Class<T> lookupType) {
                if(contains(context, name))
                {
                    return getAndReturn(name, lookupType);
                }
                else
                {
                    throw new IllegalStateException("No value for " + name);
                }
            }

            public <T> T lookup(final String name, final Class<T> lookupType, final T defaultValue) {
                if(contains(context, name))
                {
                    return getAndReturn(name, lookupType);
                }
                else
                {
                    return defaultValue;
                }
            }

            private <T> T getAndReturn(final String name, final Class<T> lookupType) {
                String value = contextLookup(context, name);
                if(String.class == lookupType)
                {
                    return (T) value;
                }
                try {
                    Method m = lookupType.getMethod("valueOf", String.class);
                    return lookupType.cast(m.invoke(null, value));
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Could not find a valueOf method on " + lookupType);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Could not invoke valueOf method on " + lookupType);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not invoke valueOf method on " + lookupType);
                }
            }
        };
    }

    private String determinePropertyName(final String name) {
        String firstChar = name.substring(3, 4);
        String newName = name.substring(4);
        return firstChar.toLowerCase() + newName;
    }

    private CtClass getDefaultsClass(final Class<T> itscoClass, final ClassPool pool) throws NotFoundException {
        for(Class<?> enclosedClass: itscoClass.getClasses())
        {
            if("Defaults".equals(enclosedClass.getSimpleName()))
            {
                return pool.get(enclosedClass.getName());
            }
        }
        throw new NotFoundException("Could not locate a defaults class for " + itscoClass);
    }

    protected abstract boolean contains(C context, String key);

    protected abstract String contextLookup(C context, String key);
}
