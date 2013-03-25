package net.peachjean.confobj.introspection;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;

public class GenericType<T> {
    private final Class<? extends T> rawType;
    private final List<GenericType<?>> parameters;
    private final String[] parameterNames;

    public <R extends T> GenericType(Class<R> rawType, GenericType<?> ... parameters) {
        this.rawType = rawType;
        this.parameters = Arrays.asList(parameters);
        TypeVariable<Class<R>>[] typeParameters = rawType.getTypeParameters();
        Validate.isTrue(parameters.length == typeParameters.length, "An invalid number of type parameters was supplied.");

        this.parameterNames = new String[typeParameters.length];
        for(int i = 0; i < parameterNames.length; i++) {
            parameterNames[i] = typeParameters[i].getName();
        }
    }

    public Class<? extends T> getRawType() {
        return rawType;
    }

    public List<GenericType<?>> getParameters() {
        return parameters;
    }

    public Type asType() {
        if(parameters.isEmpty()) {
            return this.rawType;
        } else {
            final Type[] typeArgs = new Type[parameters.size()];
            for(int i = 0; i < typeArgs.length; i++) {
                typeArgs[i] = parameters.get(i).asType();
            }
            return new ParameterizedType() {

                @Override
                public Type[] getActualTypeArguments() {
                    return typeArgs;
                }

                @Override
                public Type getRawType() {
                    return rawType;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            };
        }
    }

    public T cast(Object o) {
        return this.rawType.cast(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericType that = (GenericType) o;

        if (!parameters.equals(that.parameters)) return false;
        if (!rawType.equals(that.rawType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GenericType(( " + typeString() + " ))";
    }

    private String typeString() {
        return this.getRawType().getName() + paramsString();
    }

    private String paramsString() {
        if(this.parameters.isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder("<");
            boolean first = true;
            for(GenericType gt: this.parameters) {
                if(!first) {
                    sb.append(",");
                } else {
                    first = false;
                }
                sb.append(gt.typeString());
            }
            return sb.append(">").toString();
        }
    }

    public static <T> GenericType<T> forType(Class<T> type) {
        return forType((Type)type);
    }

    public static <T> GenericType<T> forType(Type type) {
        if(type instanceof Class) {
            return new GenericType((Class)type);
        } else if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return new GenericType<T>((Class<T>) parameterizedType.getRawType(), forTypes(parameterizedType.getActualTypeArguments()));
        } else {
            throw new IllegalArgumentException("Could not generate GenericType for " + type);
        }
    }

    public static GenericType[] forTypes(Type[] actualTypeArguments) {
        GenericType[] retVal = new GenericType[actualTypeArguments.length];
        for(int i = 0; i < actualTypeArguments.length; i++) {
            retVal[i] = forType(actualTypeArguments[i]);
        }
        return retVal;
    }

    public static <T, R extends T> GenericType<T> forTypeWithParams(Class<R> type, GenericType<?> ... params) {
        return new GenericType<T>(type, params);
    }
}
