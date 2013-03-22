package net.peachjean.itsco.support;

import javassist.*;
import javassist.bytecode.AccessFlag;
import net.peachjean.itsco.introspection.ItscoIntrospector;
import net.peachjean.itsco.introspection.ItscoVisitor;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class InlineImplementationGenerator implements ImplementationGenerator {
    public InlineImplementationGenerator() {
    }

    @Override
    public <T> Class<? extends T> implement(final Class<T> itscoClass) {
        return ItscoIntrospector.visitMembers(itscoClass, new CtClassBuilder<T>(itscoClass), new ItscoVisitor<T, CtClassBuilder<T>>() {
            @Override
            public void visitDefaults(final Class<? extends T> defaultsClass, final CtClassBuilder<T> input) {
                input.setDefaults(defaultsClass);
            }

            @Override
            public <P> void visitSimple(final String name, final Method method, final Class<P> propertyType, final boolean required, final CtClassBuilder<T> input) {
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
                input.addMemberComparison(String.format("!%s.equals(this.%s, other.%s)", ObjectUtils.class.getName(), methodCall, methodCall));
                // add toString line
                input.addToStringPair(name, methodCall);
            }

            @Override
            public <P> void visitPrimitive(final String name, final Method method, final Class<P> propertyType, final boolean required, final CtClassBuilder<T> input) {
                // create method
                final String returnType = ClassUtils.primitiveToWrapper(propertyType).getName();
                String methodBody = required
                        ? String.format("return ((%s) backer.lookup(\"%s\", %s.class)).%sValue();", returnType, name, returnType, propertyType.getName())
                        : String.format("return ((%s) backer.lookup(\"%s\", %s.class, %s.valueOf(super.%s()))).%sValue();", returnType, name, returnType, returnType, method.getName(), propertyType.getName());
                input.createMethod(method.getModifiers() & ~Modifier.ABSTRACT, propertyType, method.getName(), methodBody);

                final String methodCall = method.getName() + "()";
                // add hashCode line
                input.addHashCodeMember(String.format("%s.valueOf(%s)", returnType, methodCall));
                // add equals line
                input.addMemberComparison(String.format("this.%s != other.%s", methodCall, methodCall));
                // add toString line
                input.addToStringPair(name, methodCall);
            }

            @Override
            public <P> void visitItsco(final String name, final Method method, final Class<P> propertyType, final boolean required, final CtClassBuilder<T> input) {
                this.visitSimple(name, method, propertyType, required, input);
            }
        }).build();
    }

    private static class CtClassBuilder<T> {

        private final Class<T> itscoClass;
        private final String className;
        private CtClass implCC;
        private final ClassPool pool = ClassPool.getDefault();
        private CtClass defaultsCtClass;

        private List<String> hashCodeMembers = new ArrayList<String>();
        private List<String> memberComparisons = new ArrayList<String>();
        private Map<String, String> toStringPairs = new TreeMap<String, String>();

        public CtClassBuilder(final Class<T> itscoClass) {
            this.className = determineClassname(itscoClass);
            this.itscoClass = itscoClass;
        }

        public Class<T> build() {
            try {
                // setup hashcode method
                StringBuilder hashCodeBody = new StringBuilder("return new org.apache.commons.lang3.builder.HashCodeBuilder()");
                for (String hashCodeMember : hashCodeMembers) {
                    hashCodeBody.append(".append(" + hashCodeMember + ")");
                }
                hashCodeBody.append(".build().intValue();");
                CtClass intType = pool.get("int");
                CtMethod hashCodeMethod = CtNewMethod.make(intType, "hashCode", new CtClass[0], new CtClass[0], hashCodeBody.toString(), implCC);
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


                toStringBody.append(String.format("return \"%s{\" %n", itscoClass.getSimpleName()));
                boolean first = true;
                for (String property : toStringPairs.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        toStringBody.append(" + \", \"\n");
                    }
                    toStringBody.append(String.format(" + \"%s=\" + this.%s", property, toStringPairs.get(property)));
                }
                toStringBody.append(" + \"}\";\n");
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
            CtClass implCC = pool.makeClass(className);
            implCC.setSuperclass(defaultsCtClass);
            final CtClass itscoInterface = pool.getCtClass(itscoClass.getName());
            implCC.setInterfaces(new CtClass[]{itscoInterface});
            return implCC;
        }

        private String determineClassname(Class<T> itscoClass) {
            return itscoClass.getCanonicalName() + "$$ItscoImpl$$" + RandomStringUtils.randomAlphanumeric(7);
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