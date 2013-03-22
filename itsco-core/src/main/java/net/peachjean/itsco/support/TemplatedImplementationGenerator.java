package net.peachjean.itsco.support;

import javassist.*;
import javassist.bytecode.AccessFlag;
import net.peachjean.itsco.introspection.ItscoIntrospector;
import net.peachjean.itsco.introspection.ItscoVisitor;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class TemplatedImplementationGenerator implements ImplementationGenerator {

    private final STGroup stGroup;

    public TemplatedImplementationGenerator() {
        URL templateUrl = this.getClass().getResource("impl.stg");
        stGroup = new STGroupFile(templateUrl, Charsets.UTF_8.name(), '<', '>');
    }

    @Override
    public <T> Class<? extends T> implement(final Class<T> itscoClass) {

        return ItscoIntrospector.visitMembers(itscoClass, new CtClassBuilder<T>(itscoClass, stGroup), new ItscoVisitor<T, CtClassBuilder<T>>() {
            @Override
            public void visitDefaults(final Class<? extends T> defaultsClass, final CtClassBuilder<T> input) {
                input.setDefaults(defaultsClass);
            }

            @Override
            public <P> void visitSimple(final String name, final Method method, final Class<P> propertyType, final boolean required, final CtClassBuilder<T> input) {
                FieldModel field = new FieldModel(propertyType.getName(), name, required, method.getName(), false, null, method.getModifiers() & ~Modifier.ABSTRACT);
                input.model.getFields().add(field);
            }

            @Override
            public <P> void visitPrimitive(final String name, final Method method, final Class<P> propertyType, final boolean required, final CtClassBuilder<T> input) {

                final String returnType = ClassUtils.primitiveToWrapper(propertyType).getName();
                FieldModel field = new FieldModel(returnType, name, required, method.getName(), true, propertyType.getName(), method.getModifiers() & ~Modifier.ABSTRACT);
                input.model.getFields().add(field);
            }

            @Override
            public <P> void visitItsco(final String name, final Method method, final Class<P> propertyType, final boolean required, final CtClassBuilder<T> input) {
                this.visitSimple(name, method, propertyType, required, input);
            }
        }).build();
    }

    private static class Model {
        private String type;
        private List<ArgModel> constructorArgs = new ArrayList<ArgModel>();
        private List<FieldModel> fields = new ArrayList<FieldModel>();

        private String getType() {
            return type;
        }

        private List<ArgModel> getConstructorArgs() {
            return constructorArgs;
        }

        private List<FieldModel> getFields() {
            return fields;
        }
    }

    private static class ArgModel {
        private String type;

        private ArgModel(String type) {
            this.type = type;
        }

        private String getType() {
            return type;
        }
    }

    private static class FieldModel {
        private final String type;
        private final String name;
        private final boolean required;
        private final String methodName;
        private final boolean primitive;
        private final String primitiveType;
        private final int modifiers;

        private FieldModel(String type, String name, boolean required, String methodName, boolean primitive, String primitiveType, int modifiers) {
            this.type = type;
            this.name = name;
            this.required = required;
            this.methodName = methodName;
            this.primitive = primitive;
            this.primitiveType = primitiveType;
            this.modifiers = modifiers;
        }

        private String getType() {
            return type;
        }

        private String getName() {
            return name;
        }

        private boolean isRequired() {
            return required;
        }

        private String getMethodName() {
            return methodName;
        }

        private boolean isPrimitive() {
            return primitive;
        }

        private String getPrimitiveType() {
            return primitiveType;
        }

        private int getModifiers() {
            return modifiers;
        }
    }

    private static class CtClassBuilder<T> {

        private final Class<T> itscoClass;
        private final String className;
        private final ClassPool pool = ClassPool.getDefault();
        private CtClass defaultsCtClass;
        private final STGroup stGroup;

        private final Model model = new Model();

        public CtClassBuilder(final Class<T> itscoClass, STGroup stGroup) {
            this.stGroup = stGroup;
            this.className = determineClassname(itscoClass);
            this.itscoClass = itscoClass;
        }

        public Class<T> build() {
            try {
                CtClass implCC = setupClass(pool, defaultsCtClass);

                implCC.addField(createBackerField(implCC));

                implCC.addConstructor(createConstructor(implCC));

                for(FieldModel field: model.getFields()) {
                    implCC.addMethod(createFieldMethod(implCC, field));
                }

                // setup hashcode method
                implCC.addMethod(createHashCodeMethod(implCC));

                // setup equals method
                implCC.addMethod(createEqualsMethod(implCC));

                // setup toString method
                implCC.addMethod(createToStringMethod(implCC));

                return classFromCtClass(implCC);
            } catch (CannotCompileException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            } catch (NotFoundException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            }
        }

        private CtMethod createFieldMethod(CtClass implCC, FieldModel field) throws NotFoundException, CannotCompileException {
            CtClass returnType = pool.get(field.isPrimitive() ? field.getPrimitiveType() : field.getType());

            ST template = this.stGroup.getInstanceOf("methodBody");
            template.add("field", field);
            String methodBody = execute(template);

            CtMethod implMethod = CtNewMethod.make(
                    field.getModifiers(),
                    returnType,
                    field.getMethodName(),
                    new CtClass[0],
                    new CtClass[0],
                    methodBody,
                    implCC);
            return implMethod;
        }

        private CtConstructor createConstructor(CtClass implCC) throws NotFoundException, CannotCompileException {
            CtClass backerType = pool.get(ItscoBacker.class.getName());
            CtClass[] argTypes = new CtClass[model.getConstructorArgs().size() + 1];
            argTypes[0] = backerType;
            for(int i = 1; i < argTypes.length; i++) {
                argTypes[i] = pool.get(model.getConstructorArgs().get(i).getType());
            }
            ST template = this.stGroup.getInstanceOf("constructorBody");
            int[] args = new int[model.getConstructorArgs().size()];
            for(int i = 0; i < args.length; i++) {
                args[i] = i+2;
            }
            template.add("args", args);
            String body = execute(template);
            final CtConstructor ctConstructor = CtNewConstructor.make(
                    argTypes,
                    new CtClass[0],
                    body,
                    implCC);

            return ctConstructor;
        }

        private String execute(ST template) {
            StringWriter writer = new StringWriter();
            try {
                template.write(new AutoIndentWriter(writer, "\n"));
            } catch (IOException e) {
                throw new RuntimeException("Could not execute template.", e);
            } finally {
                IOUtils.closeQuietly(writer);
            }
            return writer.toString();
        }

        private CtField createBackerField(CtClass implCC) throws NotFoundException, CannotCompileException {
            CtClass backerType = pool.get(ItscoBacker.class.getName());
            CtField backerField = new CtField(backerType, "backer", implCC);
            backerField.setModifiers(AccessFlag.FINAL);
            return backerField;
        }

        private CtMethod createToStringMethod(CtClass implCC) throws NotFoundException, CannotCompileException {
            StringBuilder toStringBody = new StringBuilder("{\n");


            toStringBody.append(String.format("return \"%s{\" %n", itscoClass.getSimpleName()));
            boolean first = true;
            for (FieldModel field : model.getFields()) {
                if (first) {
                    first = false;
                } else {
                    toStringBody.append(" + \", \"\n");
                }
                toStringBody.append(String.format(" + \"%s=\" + this.%s()", field.getName(), field.getMethodName()));
            }
            toStringBody.append(" + \"}\";\n");
            toStringBody.append("}");
            CtClass stringType = pool.get(String.class.getName());
            return CtNewMethod.make(stringType, "toString", new CtClass[0], new CtClass[0], toStringBody.toString(), implCC);
        }

        private CtMethod createEqualsMethod(CtClass implCC) throws NotFoundException, CannotCompileException {
            StringBuilder equalsBody = new StringBuilder("{\n");
            equalsBody.append(String.format("if (!($1 instanceof %s)) { return false; }%n", itscoClass.getCanonicalName()));
            equalsBody.append(String.format("final %s that = (%s) $1;%n", itscoClass.getCanonicalName(), itscoClass.getCanonicalName()));
            for (FieldModel field : model.getFields()) {
                String comparison = field.isPrimitive()
                        ? String.format("this.%1$s() != that.%1$s()", field.methodName)
                        : String.format("!org.apache.commons.lang3.ObjectUtils.equals(this.%1$s(), that.%1$s())", field.methodName);
                equalsBody.append(String.format("if(%s) return false;", comparison));
            }
            equalsBody.append("return true;\n");
            equalsBody.append("}\n");
            CtClass boolType = pool.get("boolean");
            CtClass objectType = pool.get(Object.class.getName());
            return CtNewMethod.make(boolType, "equals", new CtClass[]{objectType}, new CtClass[0], equalsBody.toString(), implCC);
        }

        private CtMethod createHashCodeMethod(CtClass implCC) throws NotFoundException, CannotCompileException {
            StringBuilder hashCodeBody = new StringBuilder("return new org.apache.commons.lang3.builder.HashCodeBuilder()");
            for (FieldModel field : model.getFields()) {
                hashCodeBody.append(".append(this." + field.getMethodName() + "())");
            }
            hashCodeBody.append(".build().intValue();");
            CtClass intType = pool.get("int");
            return CtNewMethod.make(intType, "hashCode", new CtClass[0], new CtClass[0], hashCodeBody.toString(), implCC);
        }

        @SuppressWarnings("unchecked")
        private <T> Class<T> classFromCtClass(final CtClass implCC) throws CannotCompileException {
            return implCC.toClass();
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
                Constructor<?>[] constructors = defaultsClass.getDeclaredConstructors();
                if(constructors.length > 1) {
                    throw new IllegalStateException("A defaults class should have zero or one constructors.  " + defaultsClass + " has " + constructors.length);
                }
                if(constructors.length == 1) {
                    Constructor<?> constructor = constructors[0];
                    for(int i = 0; i < constructor.getGenericParameterTypes().length; i++) {
                        ArgModel arg = new ArgModel(constructor.getParameterTypes()[i].getName());
                    }
                }
            } catch (NotFoundException e) {
                throw new RuntimeException("Failed to build itsco class " + itscoClass, e);
            }

        }
    }
}
