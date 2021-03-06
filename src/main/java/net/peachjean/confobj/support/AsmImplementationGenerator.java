package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.BaseConfigObjectVisitor;
import net.peachjean.confobj.introspection.ConfigObjectIntrospector;
import net.peachjean.confobj.introspection.ConfigObjectVisitor;
import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureWriter;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;

/**
 * TODO:: Come back and fix error messages to be usable.
 */
class AsmImplementationGenerator implements ImplementationGenerator {

    public static final int TRUE = ICONST_1;
    public static final int FALSE = ICONST_0;
    private final ConcurrentMap<ClassLoader, AsmClassLoader> classLoaderMap = new ConcurrentHashMap();

//    @Override
    public <T> Class<? extends T> implement(Class<T> confObjType) throws ImplementationException {
        return ConfigObjectIntrospector.<T, ConfObjModel<T>>visitMembers(confObjType, new ConfObjModel<T>(confObjType, classLoaderMap), new AsmVisitor<T>()).implement();
    }

    public <T> byte[] generateByteCode(Class<T> confObjType) {
        return ConfigObjectIntrospector.visitMembers(confObjType, new ConfObjModel<T>(confObjType, classLoaderMap), new AsmVisitor<T>()).generateByteCode();
    }

    private static class ConfObjModel<T> {

        private static final Type BACKER_TYPE = Type.getObjectType(Type.getInternalName(ConfigObjectBacker.class));
        private final String randomPart = RandomStringUtils.randomAlphanumeric(6);
        private final Class<T> confObjType;
        private final ConcurrentMap<ClassLoader, AsmClassLoader> classLoaderMap;
        private Class<? extends T> defaultsClass;

        private final List<FieldModel> fields = new ArrayList<FieldModel>();

        public ConfObjModel(Class<T> confObjType, ConcurrentMap<ClassLoader, AsmClassLoader> classLoaderMap) {
            this.confObjType = confObjType;
            this.classLoaderMap = classLoaderMap;
        }

        public Class<? extends T> implement() {
            return this.getClassLoaderFor(confObjType).defineClass(this.generateImplName(), generateByteCode());
        }

        private byte[] generateByteCode() {
            Collections.sort(fields, new Comparator<FieldModel>() {
                @Override
                public int compare(FieldModel o1, FieldModel o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            cw.visit(V1_5, getClassVisibility() + ACC_SUPER, generateAsmImplName(), null, generateAsmSuperClassName(), new String[] { generateAsmInterfaceName() });

            cw.visitField(ACC_PRIVATE + ACC_FINAL, "backer", BACKER_TYPE.getDescriptor(), null, null).visitEnd();

            createConstructor(cw);

            for(FieldModel field: this.fields) {
                createFieldMethod(cw, field);
            }

            createEqualsMethod(cw);

            createHashCodeMethod(cw);

            createToStringMethod(cw);
            return cw.toByteArray();
        }

        private void createEqualsMethod(ClassWriter cw) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
            mv.visitCode();

//            if (this == o) return true;
            {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                Label l = new Label();
                mv.visitJumpInsn(IF_ACMPNE, l);
                mv.visitInsn(TRUE);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l);
            }
//            if (o == null) return false;
            {
                mv.visitVarInsn(ALOAD, 1);
                Label l = new Label();
                mv.visitJumpInsn(IFNONNULL, l);
                mv.visitInsn(FALSE);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l);
            }
//            if (! (o instanceof ExampleConfigObject)) return false;
            {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(confObjType));
                Label l = new Label();
                mv.visitJumpInsn(IFNE, l);
                mv.visitInsn(FALSE);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l);
            }

//            ExampleConfigObject that = (ExampleConfigObject) o;
            {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(confObjType));
                mv.visitVarInsn(ASTORE, 2);
            }
//            if (getIntValue() != null ? !getIntValue().equals(that.getIntValue()) : that.getIntValue() != null) return false;
//            if (getValue1() != null ? !getValue1().equals(that.getValue1()) : that.getValue1() != null) return false;
//            if (getValue2() != null ? !getValue2().equals(that.getValue2()) : that.getValue2() != null) return false;
            for(FieldModel field: fields) {
                field.doEqualsCompare(mv, confObjType);
            }
//            return true;
            mv.visitInsn(TRUE);
            mv.visitInsn(IRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createHashCodeMethod(ClassWriter cw) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
            mv.visitCode();
//            int result = 0;
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 1);
//            result = 31 * result + (getValue1() != null ? getValue1().hashCode() : 0);
//            result = 31 * result + (getValue2() != null ? getValue2().hashCode() : 0);
//            result = 31 * result + (getIntValue() != null ? getIntValue().hashCode() : 0);
            for(FieldModel field: fields) {
                field.doHashCode(mv, confObjType);
            }
//            return result;
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(IRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createToStringMethod(ClassWriter cw) {
//            return "ExampleConfigObject{" +
//                    "value1='" + getValue1() + '\'' +
//                    ", value2='" + getValue2() + '\'' +
//                    ", intValue=" + getIntValue() +
//                    '}';
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
            mv.visitCode();

// StringBuilder sb = new StringBuilder("ExampleConfigObject");
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(confObjType.getSimpleName());
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
            mv.visitVarInsn(ASTORE, 1);
            mv.visitVarInsn(ALOAD, 1);

// sb.append("{");
            mv.visitLdcInsn("{");
            appendLast(mv);
            mv.visitInsn(POP);

            boolean first = true;
            for(FieldModel field: fields) {
                // sb.append("value1=").append(getValue1());
                mv.visitVarInsn(ALOAD, 1);
                if(!first) {
                    mv.visitLdcInsn(", ");
                    appendLast(mv);
                } else {
                    first = false;
                }
                mv.visitLdcInsn(field.name + "=");
                appendLast(mv);
                mv.visitVarInsn(ALOAD, 0);
                field.invoke(mv, confObjType);
                appendLast(mv, field.primitiveType == null ? Object.class : field.primitiveType);
//                appendLast(mv);
                mv.visitInsn(POP);
            }
// sb.append("}");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("}");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);

// return sb.toString();
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void appendLast(MethodVisitor mv) {
            this.appendLast(mv, Object.class);
        }

        private void appendLast(MethodVisitor mv, Class type) {
            // append(byte) and append(short) don't exist - we need to use append(int) instead
            String descriptor = (type == byte.class || type == short.class) ? "I" : Type.getDescriptor(type);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + descriptor + ")Ljava/lang/StringBuilder;");
        }

        private void createFieldMethod(ClassWriter cw, FieldModel field) {
            String genericTypeSignature = Type.getDescriptor(GenericType.class);

            String returnType = Type.getDescriptor(field.primitiveType == null ? field.type : field.primitiveType);
            String signature = field.primitiveType == null ? generateGetterSignature(field.genericType) : null; // we don't care about signature if return type is primitive
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, field.methodName, "()" + returnType, signature, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, generateAsmImplName(), "backer", BACKER_TYPE.getDescriptor());
            mv.visitLdcInsn(field.name);
            instantiateGenericType(field, mv);

            if(field.required) {
                // backer.lookup(String, Class)
                mv.visitMethodInsn(INVOKEINTERFACE, BACKER_TYPE.getInternalName(), "lookup", "(Ljava/lang/String;" + genericTypeSignature + ")Ljava/lang/Object;");
            } else {
                // super.getXXX()
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, generateAsmSuperClassName(), field.methodName, "()" + returnType);

                // box primitive
                if(field.primitiveType != null) {
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(field.type), "valueOf", "(" + Type.getDescriptor(field.primitiveType) + ")" + Type.getDescriptor(field.type));
                }
                // backer.lookup(String, Class, T)
                mv.visitMethodInsn(INVOKEINTERFACE, BACKER_TYPE.getInternalName(), "lookup", "(Ljava/lang/String;" + genericTypeSignature + "Ljava/lang/Object;)Ljava/lang/Object;");
            }
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(field.type));

            if(field.primitiveType != null) {
                // unbox
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(field.type), field.primitiveType + "Value", "()" + Type.getDescriptor(field.primitiveType));
                mv.visitInsn(getReturnType(field.primitiveType));
            } else {
                mv.visitInsn(getReturnType(field.type));
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private String generateConstructorSignature(GenericType ... parameterTypes) {
            SignatureWriter sw = new SignatureWriter();
            sw.visitParameterType();
            sw.visitClassType(Type.getInternalName(ConfigObjectBacker.class));
            sw.visitEnd();
            for(GenericType parameterType: parameterTypes) {
                sw.visitParameterType();
                generateGenericTypeSignature(sw, parameterType);
                sw.visitEnd();
            }
            sw.visitReturnType();
            sw.visitBaseType('V');
            sw.visitEnd();
            return sw.toString();
        }
        private String generateGetterSignature(GenericType genericType) {
            SignatureWriter sw = new SignatureWriter();
            sw.visitReturnType();
            generateGenericTypeSignature(sw, genericType);
            sw.visitEnd();
            return sw.toString();
        }

        private void generateGenericTypeSignature(SignatureWriter sw, GenericType<?> genericType) {
            sw.visitClassType(Type.getInternalName(genericType.getRawType()));
            if(!genericType.getParameters().isEmpty()) {
                sw.visitTypeArgument('=');
                for(GenericType<?> parameter: genericType.getParameters()) {
                    generateGenericTypeSignature(sw, parameter);
                }
                sw.visitEnd();
            }
        }

        private void instantiateGenericType(FieldModel field, MethodVisitor mv) {
            instantiateGenericType(mv, field.genericType);
        }

        private void instantiateGenericType(MethodVisitor mv, GenericType genericType) {
            mv.visitLdcInsn(Type.getType(genericType.getRawType()));
            List<GenericType<?>> parameters = genericType.getParameters();
            mv.visitIntInsn(BIPUSH, parameters.size());
            mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(GenericType.class));

            int index = 0;
            for(GenericType<?> parameter: parameters) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, index++);
                instantiateGenericType(mv, parameter);
                mv.visitInsn(AASTORE);
            }
            String gtSig = Type.getDescriptor(GenericType.class);
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(GenericType.class), "forTypeWithParams", "(Ljava/lang/Class;[" + gtSig + ")" + gtSig);
        }

        private int getReturnType(Class type) {
            if(type.isPrimitive()) {
                if(type.equals(double.class)) {
                    return DRETURN;
                } else if(type.equals(float.class)) {
                    return FRETURN;
                } else if(type.equals(long.class)) {
                    return LRETURN;
                } else {
                    return IRETURN;
                }
            } else {
                return ARETURN;
            }
        }

        private void createConstructor(ClassWriter cw) {
            StringBuilder sb = new StringBuilder("(").append(BACKER_TYPE.getDescriptor());


            String constructorDescriptor = Type.getConstructorDescriptor(getDefaultsConstructor());
            Type[] argumentTypes = Type.getArgumentTypes(constructorDescriptor);
            for (Type constructorArg : argumentTypes) {
                sb.append(constructorArg.getDescriptor());
            }
            sb.append(")V");
            String signature = generateConstructorSignature(GenericType.forTypes(getDefaultsConstructor().getGenericParameterTypes()));
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", sb.toString(), signature, null);
            mv.visitCode();
            // super(x, y, z)
            mv.visitVarInsn(ALOAD, 0);
            for(int i = 0; i < argumentTypes.length; i++) {
                mv.visitVarInsn(ALOAD, i + 2 ); // +2 to skip 0 and the backer arg
            }
            mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(defaultsClass), "<init>", constructorDescriptor);

            // this.backer = backer
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, generateAsmImplName(), "backer", BACKER_TYPE.getDescriptor());
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEINTERFACE, BACKER_TYPE.getInternalName(), "setContaining", "(Ljava/lang/Object;)V");

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private AsmClassLoader getClassLoaderFor(Class<?> confObjType) {
            ClassLoader interfaceCl = confObjType.getClassLoader();
            if(!this.classLoaderMap.containsKey(interfaceCl)) {
                this.classLoaderMap.putIfAbsent(interfaceCl, new AsmClassLoader(interfaceCl));
            }
            return this.classLoaderMap.get(interfaceCl);
        }

        private String generateAsmSuperClassName() {
            return Type.getInternalName(defaultsClass != null ? defaultsClass : Object.class);
        }

        private String generateAsmInterfaceName() {
            return Type.getInternalName(confObjType);
        }

        private String generateAsmImplName() {
            return this.generateImplName().replaceAll("\\.", "/");
//            return Type.getInternalName(this.generateImplName());
        }

        private String generateImplName() {
            return this.confObjType.getName() + "$$ConfigObject$$" + randomPart;
        }

        private int getClassVisibility() {
            if(Modifier.isPublic(this.confObjType.getModifiers())) {
                return ACC_PUBLIC;
            } else {
                return 0;
            }
        }

        public Constructor getDefaultsConstructor() {
            if(defaultsClass.getDeclaredConstructors().length == 0) {
                try {
                    return defaultsClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("There must be at least a no-arg constructor.", e);
                }
            } else if(defaultsClass.getDeclaredConstructors().length > 1) {
                throw new IllegalStateException("A Defaults class must define no more than a single constructor.");
            } else {
                return defaultsClass.getDeclaredConstructors()[0];
            }
        }
    }

    private static class FieldModel {
        public final String methodName;
        public final GenericType genericType;
        public final Class type;
        public final String name;
        public final boolean required;
        public final Class primitiveType;

        private FieldModel(String methodName, GenericType type, String name, boolean required) {
            this.methodName = methodName;
            this.name = name;
            this.required = required;
            if(type.getRawType().isPrimitive()) {
                this.type = ClassUtils.primitiveToWrapper(type.getRawType());
                this.primitiveType = type.getRawType();
                this.genericType = GenericType.forType(this.type);
            } else {
                this.type = type.getRawType();
                this.genericType = type;
                this.primitiveType = null;
            }
        }

        public void invoke(MethodVisitor mv, Class confObjType) {
            if(this.primitiveType == null) {
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(confObjType), methodName, "()" + Type.getDescriptor(type));
            } else {
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(confObjType), methodName, "()" + Type.getDescriptor(primitiveType));
            }
        }

        public void invokeEquals(MethodVisitor mv) {
            if(this.type.isInterface()) {
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(type), "equals", "(Ljava/lang/Object;)Z");
            } else {
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(type), "equals", "(Ljava/lang/Object;)Z");
            }
        }

        public void invokeHashCode(MethodVisitor mv) {
            if(this.type.isInterface()) {
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(type), "hashCode", "()I");
            } else {
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(type), "hashCode", "()I");
            }
        }

        public void doEqualsCompare(MethodVisitor mv, Class confObjType) {
            if(this.primitiveType != null) {
                mv.visitVarInsn(ALOAD, 0);
                this.invoke(mv, confObjType);
                mv.visitVarInsn(ALOAD, 2);
                this.invoke(mv, confObjType);
                Label l = new Label();
                if(this.primitiveType.equals(double.class)) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "compare", "(DD)I");
                    mv.visitJumpInsn(IFEQ, l);
                } else if(this.primitiveType.equals(float.class)) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "compare", "(FF)I");
                    mv.visitJumpInsn(IFEQ, l);
                } else if(this.primitiveType.equals(long.class)) {
                    mv.visitInsn(LCMP);
                    mv.visitJumpInsn(IFEQ, l);
                } else {
                    mv.visitJumpInsn(IF_ICMPEQ, l);
                }
                mv.visitInsn(FALSE);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l);
            } else {
                mv.visitVarInsn(ALOAD, 0);
                this.invoke(mv, confObjType);
                Label l1 = new Label();
                mv.visitJumpInsn(IFNULL, l1);
                mv.visitVarInsn(ALOAD, 0);
                this.invoke(mv, confObjType);
                mv.visitVarInsn(ALOAD, 2);
                this.invoke(mv, confObjType);
                this.invokeEquals(mv);
                Label l2 = new Label();
                mv.visitJumpInsn(IFNE, l2);
                Label l3 = new Label();
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l1);
                mv.visitVarInsn(ALOAD, 2);
                this.invoke(mv, confObjType);
                mv.visitJumpInsn(IFNONNULL, l2);
                mv.visitLabel(l3);
                mv.visitInsn(FALSE);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l2);
            }
        }

        public void doHashCode(MethodVisitor mv, Class confObjType) {
            if(primitiveType != null) {
                if(primitiveType.equals(boolean.class)) {
                    mv.visitIntInsn(BIPUSH, 31);
                    mv.visitVarInsn(ILOAD, 1);
                    mv.visitInsn(IMUL);
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    Label l0 = new Label();
                    mv.visitJumpInsn(IFEQ, l0);
                    mv.visitInsn(ICONST_1);
                    Label l1 = new Label();
                    mv.visitJumpInsn(GOTO, l1);
                    mv.visitLabel(l0);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(l1);
                    mv.visitInsn(IADD);
                    mv.visitVarInsn(ISTORE, 1);
                } else if(primitiveType.equals(long.class)) {
                    mv.visitIntInsn(BIPUSH, 31);
                    mv.visitVarInsn(ILOAD, 1);
                    mv.visitInsn(IMUL);
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    mv.visitIntInsn(BIPUSH, 32);
                    mv.visitInsn(LUSHR);
                    mv.visitInsn(LXOR);
                    mv.visitInsn(L2I);
                    mv.visitInsn(IADD);
                    mv.visitVarInsn(ISTORE, 1);
                } else if(primitiveType.equals(float.class)) {
                    mv.visitIntInsn(BIPUSH, 31);
                    mv.visitVarInsn(ILOAD, 1);
                    mv.visitInsn(IMUL);
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    mv.visitInsn(FCONST_0);
                    mv.visitInsn(FCMPL);
                    Label l0 = new Label();
                    mv.visitJumpInsn(IFEQ, l0);
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I");
                    Label l1 = new Label();
                    mv.visitJumpInsn(GOTO, l1);
                    mv.visitLabel(l0);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(l1);
                    mv.visitInsn(IADD);
                    mv.visitVarInsn(ISTORE, 1);
                } else if(primitiveType.equals(double.class)) {
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    mv.visitInsn(DCONST_0);
                    mv.visitInsn(DCMPL);
                    Label l4 = new Label();
                    mv.visitJumpInsn(IFEQ, l4);
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J");
                    Label l5 = new Label();
                    mv.visitJumpInsn(GOTO, l5);
                    mv.visitLabel(l4);
                    mv.visitInsn(LCONST_0);
                    mv.visitLabel(l5);
                    mv.visitVarInsn(LSTORE, 2);
                    mv.visitVarInsn(BIPUSH, 31);
                    mv.visitIntInsn(ILOAD, 1);
                    mv.visitInsn(IMUL);
                    mv.visitVarInsn(LLOAD, 2);
                    mv.visitVarInsn(LLOAD, 2);
                    mv.visitIntInsn(BIPUSH, 32);
                    mv.visitInsn(LUSHR);
                    mv.visitInsn(LXOR);
                    mv.visitInsn(L2I);
                    mv.visitInsn(IADD);
                    mv.visitVarInsn(ISTORE, 1);
                } else {
                    mv.visitIntInsn(BIPUSH, 31);
                    mv.visitVarInsn(ILOAD, 1);
                    mv.visitInsn(IMUL);
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv, confObjType);
                    mv.visitInsn(IADD);
                    mv.visitVarInsn(ISTORE, 1);
                }
            } else {
                mv.visitIntInsn(BIPUSH, 31);
                mv.visitVarInsn(ILOAD, 1);
                mv.visitInsn(IMUL);
                mv.visitVarInsn(ALOAD, 0);
                this.invoke(mv, confObjType);
                Label l0 = new Label();
                mv.visitJumpInsn(IFNULL, l0);
                mv.visitVarInsn(ALOAD, 0);
                this.invoke(mv, confObjType);
                this.invokeHashCode(mv);
                Label l1 = new Label();
                mv.visitJumpInsn(GOTO, l1);
                mv.visitLabel(l0);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(l1);
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, 1);
            }
        }
    }

    private class AsmVisitor<T> extends BaseConfigObjectVisitor<T,ConfObjModel<T>> {//
//        @Override
        public <P> void visitSimple(String name, Method method, GenericType<P> propertyType, boolean required, ConfObjModel<T> input) {
            input.fields.add(new FieldModel(method.getName(), propertyType, name, required));
        }

//        @Override
        public <P> void visitConfigObject(String name, Method method, GenericType<P> propertyType, boolean required, ConfObjModel<T> input) {
            input.fields.add(new FieldModel(method.getName(), propertyType, name, required));
        }

//        @Override
        public <P> void visitPrimitive(String name, Method method, Class<P> propertyType, boolean required, ConfObjModel<T> input) {
            input.fields.add(new FieldModel(method.getName(), GenericType.forType(propertyType), name, required));
        }

//        @Override
        public void visitDefaults(Class<? extends T> defaultsClass, ConfObjModel<T> input) {
            input.defaultsClass = defaultsClass;
        }
    }

    private static class AsmClassLoader extends ClassLoader {

        private AsmClassLoader(ClassLoader parent) {
            super(parent);
        }

        @SuppressWarnings("unchecked")
        public <T> Class<? extends T> defineClass(String name, byte[] bytes) {
            return (Class<? extends T>) super.defineClass(name, bytes, 0, bytes.length);
        }


    }
}
