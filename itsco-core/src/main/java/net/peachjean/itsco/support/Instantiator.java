package net.peachjean.itsco.support;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javassist.*;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Descriptor;
import net.peachjean.itsco.introspection.ItscoIntrospector;
import net.peachjean.itsco.introspection.ItscoVisitor;
import org.apache.commons.lang.RandomStringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Instantiator {
    private final LoadingCache<Class, Function<ItscoBacker, Object>> cache = CacheBuilder.newBuilder().build(new ImplementationGenerator());

    @SuppressWarnings("unchecked")
    <T> Function<ItscoBacker, T> lookupFunction(final Class<T> itscoInterface) {
        try {
            return (Function<ItscoBacker, T>) cache.get(itscoInterface);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to instantiate itsco " + itscoInterface.getName(), e);
        }
    }

    private class ImplementationGenerator extends CacheLoader<Class, Function<ItscoBacker, Object>> {
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
            return ItscoIntrospector.visitMembers(itscoClass, new CtClassBuilder<T>(itscoClass), new ItscoVisitor<T, CtClassBuilder<T>>() {
                @Override
                public void visitDefaults(final Class<? extends T> defaultsClass, final CtClassBuilder<T> input) {
                    input.setDefaults(defaultsClass);
                }

                @Override
                public void visitSimple(final String name, final Method method, final Class<?> propertyType, final boolean required, final CtClassBuilder<T> input) {
                    // create method
                    final String returnType = propertyType.getName();
                    String methodBody = required
                            ? String.format("return (%s) backer.lookup(\"%s\", %s.class);", returnType, name, returnType)
                            : String.format("return (%s) backer.lookup(\"%s\", %s.class, super.%s());", returnType, name, returnType, method.getName());
                    input.createMethod(method.getModifiers() & ~Modifier.ABSTRACT, propertyType, method.getName(), methodBody);

                    final String methodCall = method.getName() + "()";
                    // add hashCode line
                    input.addHashCodeMember(methodCall);
                    // add equals line
                    input.addMemberComparison(String.format("!%s.equal(this.%s, other.%s)", Objects.class.getName(), methodCall, methodCall));
                    // add toString line
                    input.addToStringPair(name, methodCall);

                }

                @Override
                public void visitItsco(final String name, final Method method, final Class<?> propertyType, final boolean required, final CtClassBuilder<T> input) {
                    this.visitSimple(name, method, propertyType, required, input);
                }
            }).build();
        }
    }

    private static class CtClassBuilder<T> {

        private final Class<T> itscoClass;
        private CtClass implCC;
        private final ClassPool pool = ClassPool.getDefault();
        private CtClass defaultsCtClass;

        private List<String> hashCodeMembers = Lists.newArrayList();
        private List<String> memberComparisons = Lists.newArrayList();
        private Map<String, String> toStringPairs = Maps.newTreeMap();

        public CtClassBuilder(final Class<T> itscoClass) {
            this.itscoClass = itscoClass;
        }

        public Class<T> build() {
            try {
                // setup hashcode method
                String hashCodeBody = String.format("return com.google.common.base.Objects.hashCode(new Object[] {%s});", Joiner.on(",").join(hashCodeMembers));
                CtClass intType = pool.get("int");
                CtMethod hashCodeMethod = CtNewMethod.make(intType, "hashCode", new CtClass[0], new CtClass[0], hashCodeBody, implCC);
                implCC.addMethod(hashCodeMethod);

                // setup equals method
                StringBuilder equalsBody = new StringBuilder("{\n");
                equalsBody.append(String.format("if (!($1 instanceof %s)) { return false; }%n", itscoClass.getCanonicalName()));
                equalsBody.append(String.format("final %s other = (%s) $1;%n", itscoClass.getCanonicalName(), itscoClass.getCanonicalName()));
                for (String memberComparison : memberComparisons) {
                    equalsBody.append(String.format("if(%s) { return false; }%n", memberComparison));
                }
                equalsBody.append("return true;\n");
                equalsBody.append("}\n");
                CtClass boolType = pool.get("boolean");
                CtClass objectType = pool.get(Object.class.getName());
                CtMethod equalsMethod = CtNewMethod.make(boolType, "equals", new CtClass[]{objectType}, new CtClass[0], equalsBody.toString(), implCC);
                implCC.addMethod(equalsMethod);

                // setup toString method
                StringBuilder toStringBody = new StringBuilder("{\n");
                toStringBody.append(String.format("return %s.toStringHelper(%s.class)%n", Objects.class.getName(), itscoClass.getCanonicalName()));
                for (String property : toStringPairs.keySet()) {
                    toStringBody.append(String.format(".add(\"%s\", this.%s)%n", property, toStringPairs.get(property)));
                }
                toStringBody.append(".toString();\n");
                toStringBody.append("}");
                CtClass stringType = pool.get(String.class.getName());
                CtMethod toStringMethod = CtNewMethod.make(stringType, "toString", new CtClass[0], new CtClass[0], toStringBody.toString(), implCC);
                implCC.addMethod(toStringMethod);

                return classFromCtClass(implCC);
            } catch (CannotCompileException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            } catch (NotFoundException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            }
        }

        @SuppressWarnings("unchecked")
        private <T> Class<T> classFromCtClass(final CtClass implCC) throws CannotCompileException {
            return implCC.toClass();
        }

        private void setupConstructor(final CtClass implCC, final CtClass backerType) throws CannotCompileException {
            final CtConstructor ctConstructor = CtNewConstructor.make(
                    new CtClass[]{backerType},
                    new CtClass[0],
                    "this.backer = $1;",
                    implCC);

            implCC.addConstructor(ctConstructor);
        }

        private CtClass setupBackerField(final ClassPool pool, final CtClass implCC) throws NotFoundException, CannotCompileException {
            CtClass backerType = pool.get(ItscoBacker.class.getName());
            CtField backerField = new CtField(backerType, "backer", implCC);
            backerField.setModifiers(AccessFlag.FINAL);
            implCC.addField(backerField);
            return backerType;
        }

        private CtClass setupClass(final ClassPool pool, final CtClass defaultsCtClass) throws NotFoundException, CannotCompileException {
            CtClass implCC = pool.makeClass(itscoClass.getCanonicalName() + "$$ItscoImpl$$" + RandomStringUtils.randomAlphanumeric(7));
            implCC.setSuperclass(defaultsCtClass);
            final CtClass itscoInterface = pool.getCtClass(itscoClass.getName());
            implCC.setInterfaces(new CtClass[]{itscoInterface});
            return implCC;
        }

        public void setDefaults(final Class<? extends T> defaultsClass) {
            try {
                defaultsCtClass = pool.get(defaultsClass.getName());
                implCC = setupClass(pool, defaultsCtClass);
                CtClass backerType = setupBackerField(pool, implCC);
                setupConstructor(implCC, backerType);


            } catch (NotFoundException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            } catch (CannotCompileException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            }

        }

        public void createMethod(final int modifiers, final Class<?> propertyType, final String methodName, final String methodBody) {
            try {
                CtClass returnType = pool.get(propertyType.getName());
                CtMethod implMethod = CtNewMethod.make(
                        modifiers,
                        returnType,
                        methodName,
                        new CtClass[0],
                        new CtClass[0],
                        methodBody,
                        implCC);
                implCC.addMethod(implMethod);
            } catch (NotFoundException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            } catch (CannotCompileException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            }
        }

        public void addHashCodeMember(final String member) {
            hashCodeMembers.add(member);
        }

        public void addMemberComparison(final String comparison) {
            memberComparisons.add(comparison);
        }

        public void addToStringPair(final String name, final String methodCall) {
            toStringPairs.put(name, methodCall);
        }
    }
}
