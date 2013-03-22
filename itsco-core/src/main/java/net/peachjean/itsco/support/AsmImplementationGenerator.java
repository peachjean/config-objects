package net.peachjean.itsco.support;

import net.peachjean.itsco.introspection.ItscoIntrospector;
import net.peachjean.itsco.introspection.ItscoVisitor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;

/**
 * TODO:: Come back and fix error messages to be usable.
 */
public class AsmImplementationGenerator implements ImplementationGenerator {

    public static final int TRUE = ICONST_1;
    public static final int FALSE = ICONST_0;
    private final ConcurrentMap<ClassLoader, AsmClassLoader> classLoaderMap = new ConcurrentHashMap();

//    @Override
    public <T> Class<? extends T> implement(Class<T> itscoClass) throws ImplementationException {
        return ItscoIntrospector.<T, ItscoModel<T>>visitMembers(itscoClass, new ItscoModel<T>(itscoClass, classLoaderMap), new AsmVisitor<T>()).implement();
    }

    private static class ItscoModel<T> {

        private static final Type BACKER_TYPE = Type.getObjectType(Type.getInternalName(ItscoBacker.class));
        private final String randomPart = RandomStringUtils.randomAlphanumeric(6);
        private final Class<T> itscoClass;
        private final ConcurrentMap<ClassLoader, AsmClassLoader> classLoaderMap;
        private Class<? extends T> defaultsClass;

        private final List<FieldModel> fields = new ArrayList<FieldModel>();

        public ItscoModel(Class<T> itscoClass, ConcurrentMap<ClassLoader, AsmClassLoader> classLoaderMap) {
            this.itscoClass = itscoClass;
            this.classLoaderMap = classLoaderMap;
        }

        public Class<? extends T> implement() {
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

            return this.getClassLoaderFor(itscoClass).defineClass(this.generateImplName(), cw.toByteArray());
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
//            if (! (o instanceof ExampleItsco)) return false;
            {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(itscoClass));
                Label l = new Label();
                mv.visitJumpInsn(IFNE, l);
                mv.visitInsn(FALSE);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l);
            }

//            ExampleItsco that = (ExampleItsco) o;
            {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(itscoClass));
                mv.visitVarInsn(ASTORE, 2);
            }
//            if (getIntValue() != null ? !getIntValue().equals(that.getIntValue()) : that.getIntValue() != null) return false;
//            if (getValue1() != null ? !getValue1().equals(that.getValue1()) : that.getValue1() != null) return false;
//            if (getValue2() != null ? !getValue2().equals(that.getValue2()) : that.getValue2() != null) return false;
            for(FieldModel field: fields) {
                mv.visitVarInsn(ALOAD, 0);
                field.invoke(mv, itscoClass);
                Label l1 = new Label();
                mv.visitJumpInsn(IFNULL, l1);
                mv.visitVarInsn(ALOAD, 0);
                field.invoke(mv, itscoClass);
                mv.visitVarInsn(ALOAD, 2);
                field.invoke(mv, itscoClass);
                field.invokeEquals(mv);
                Label l2 = new Label();
                mv.visitJumpInsn(IFNE, l2);
                Label l3 = new Label();
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l1);
                mv.visitVarInsn(ALOAD, 2);
                field.invoke(mv, itscoClass);
                mv.visitJumpInsn(IFNONNULL, l2);
                mv.visitLabel(l3);
                mv.visitInsn(FALSE);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l2);
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
                mv.visitIntInsn(BIPUSH, 31);
                mv.visitVarInsn(ILOAD, 1);
                mv.visitInsn(IMUL);
                mv.visitVarInsn(ALOAD, 0);
                field.invoke(mv, itscoClass);
                Label l0 = new Label();
                mv.visitJumpInsn(IFNULL, l0);
                mv.visitVarInsn(ALOAD, 0);
                field.invoke(mv, itscoClass);
                field.invokeHashCode(mv);
                Label l1 = new Label();
                mv.visitJumpInsn(GOTO, l1);
                mv.visitLabel(l0);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(l1);
                mv.visitInsn(IADD);
                mv.visitVarInsn(ISTORE, 1);
            }
//            return result;
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(IRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createToStringMethod(ClassWriter cw) {
//            return "ExampleItsco{" +
//                    "value1='" + getValue1() + '\'' +
//                    ", value2='" + getValue2() + '\'' +
//                    ", intValue=" + getIntValue() +
//                    '}';
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
            mv.visitCode();

// StringBuilder sb = new StringBuilder("ExampleItsco");
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(itscoClass.getSimpleName());
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
                mv.visitLdcInsn("value1=");
                appendLast(mv);
                mv.visitVarInsn(ALOAD, 0);
                field.invoke(mv, itscoClass);
                appendLast(mv);
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
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
        }

        private void createFieldMethod(ClassWriter cw, FieldModel field) {
            String returnType = Type.getDescriptor(field.primitiveType == null ? field.type : field.primitiveType);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, field.methodName, "()" + returnType, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, generateAsmImplName(), "backer", BACKER_TYPE.getDescriptor());
            mv.visitLdcInsn(field.name);
            mv.visitLdcInsn(Type.getType(field.type));

            if(field.required) {
                // backer.lookup(String, Class)
                mv.visitMethodInsn(INVOKEINTERFACE, BACKER_TYPE.getInternalName(), "lookup", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
            } else {
                // super.getXXX()
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, generateAsmSuperClassName(), field.methodName, "()" + returnType);

                // box primitive
                if(field.primitiveType != null) {
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(field.type), "valueOf", "(" + Type.getDescriptor(field.primitiveType) + ")" + Type.getDescriptor(field.type));
                }
                // backer.lookup(String, Class, T)
                mv.visitMethodInsn(INVOKEINTERFACE, BACKER_TYPE.getInternalName(), "lookup", "(Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;");
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
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", sb.toString(), null, null);
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

        private AsmClassLoader getClassLoaderFor(Class<?> itscoInterface) {
            ClassLoader interfaceCl = itscoInterface.getClassLoader();
            if(!this.classLoaderMap.containsKey(interfaceCl)) {
                this.classLoaderMap.putIfAbsent(interfaceCl, new AsmClassLoader(interfaceCl));
            }
            return this.classLoaderMap.get(interfaceCl);
        }

        private String generateAsmSuperClassName() {
            return Type.getInternalName(defaultsClass != null ? defaultsClass : Object.class);
        }

        private String generateAsmInterfaceName() {
            return Type.getInternalName(itscoClass);
        }

        private String generateAsmImplName() {
            return this.generateImplName().replaceAll("\\.", "/");
//            return Type.getInternalName(this.generateImplName());
        }

        private String generateImplName() {
            return this.itscoClass.getName() + "$$Itsco$$" + randomPart;
        }

        private int getClassVisibility() {
            if(Modifier.isPublic(this.itscoClass.getModifiers())) {
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
        public final Class type;
        public final String name;
        public final boolean required;
        public final Class primitiveType;

        private FieldModel(String methodName, Class type, String name, boolean required) {
            this.methodName = methodName;
            this.name = name;
            this.required = required;
            if(type.isPrimitive()) {
                this.type = ClassUtils.primitiveToWrapper(type);
                this.primitiveType = type;
            } else {
                this.type = type;
                this.primitiveType = null;
            }
        }

        public void invoke(MethodVisitor mv, Class itscoType) {
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(itscoType), methodName, "()" + Type.getDescriptor(type));
        }

        public void invokeEquals(MethodVisitor mv) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(type), "equals", "(Ljava/lang/Object;)Z");
        }

        public void invokeHashCode(MethodVisitor mv) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(type), "hashCode", "()I");
        }
    }

    private class AsmVisitor<T> implements ItscoVisitor<T,ItscoModel<T>> {//
//        @Override
        public <P> void visitSimple(String name, Method method, Class<P> propertyType, boolean required, ItscoModel<T> input) {
            input.fields.add(new FieldModel(method.getName(), propertyType, name, required));
        }

//        @Override
        public <P> void visitItsco(String name, Method method, Class<P> propertyType, boolean required, ItscoModel<T> input) {
            input.fields.add(new FieldModel(method.getName(), propertyType, name, required));
        }

//        @Override
        public <P> void visitPrimitive(String name, Method method, Class<P> propertyType, boolean required, ItscoModel<T> input) {
            input.fields.add(new FieldModel(method.getName(), propertyType, name, required));
        }

//        @Override
        public void visitDefaults(Class<? extends T> defaultsClass, ItscoModel<T> input) {
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